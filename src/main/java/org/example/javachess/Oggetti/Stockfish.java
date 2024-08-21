package org.example.javachess.Oggetti;

import javafx.application.Platform;
import javafx.scene.control.Label;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.Square;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.example.javachess.Utils.ChessMoveConverter;

public class Stockfish {
    private Process process1; // Per la valutazione della posizione
    private Process process2; // Per il calcolo delle migliori mosse
    private BufferedReader reader1;
    private BufferedReader reader2;
    private OutputStreamWriter writer1;
    private OutputStreamWriter writer2;
    private ChessMoveConverter ChessMoveConverter = new ChessMoveConverter();

    public Stockfish(String stockfishPath) {
        try {
            // Inizializza il primo processo di Stockfish
            ProcessBuilder pb1 = new ProcessBuilder(stockfishPath);
            process1 = pb1.start();
            reader1 = new BufferedReader(new InputStreamReader(process1.getInputStream()));
            writer1 = new OutputStreamWriter(process1.getOutputStream());

            writer1.write("uci\n");
            writer1.flush();

            String line;
            while ((line = reader1.readLine()) != null) {
                if (line.equals("uciok")) {
                    break;
                }
            }

            // Inizializza il secondo processo di Stockfish
            ProcessBuilder pb2 = new ProcessBuilder(stockfishPath);
            process2 = pb2.start();
            reader2 = new BufferedReader(new InputStreamReader(process2.getInputStream()));
            writer2 = new OutputStreamWriter(process2.getOutputStream());

            writer2.write("uci\n");
            writer2.flush();

            while ((line = reader2.readLine()) != null) {
                if (line.equals("uciok")) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void evaluatePosition(String fen, Label evaluationLabel) {
        new Thread(() -> {
            try {
                writer1.write("position fen " + fen + "\n");
                writer1.flush();

                writer1.write("go depth 18\n");
                writer1.flush();

                String line;
                while ((line = reader1.readLine()) != null) {
                    if (line.startsWith("info depth ")) {
                        String[] parts = line.split(" ");
                        final int depth = Integer.parseInt(parts[2]);
                        final int scoreIndex = Arrays.asList(parts).indexOf("score");

                        if (scoreIndex != -1) {
                            final String scoreType = parts[scoreIndex + 1];

                            if (scoreType.equals("cp")) {
                                final double score = Integer.parseInt(parts[scoreIndex + 2]) / 100.0;
                                final double adjustedScore = fen.contains(" b ") ? -score : score;
                                Platform.runLater(() -> evaluationLabel.setText("Depth " + depth + ": " + adjustedScore));

                            } else if (scoreType.equals("mate")) {
                                final int mateIn = Integer.parseInt(parts[scoreIndex + 2]);
                                final String mateText = mateIn > 0 ? "#" + mateIn : "#-" + Math.abs(mateIn);
                                Platform.runLater(() -> evaluationLabel.setText("Depth " + depth + ": " + mateText));
                            }
                        }
                    } else if (line.startsWith("bestmove")) {
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void getTopThreeMoves(String fen, Label move1Label, Label move2Label, Label move3Label) {
        new Thread(() -> {
            try {
                writer2.write("setoption name MultiPV value 3\n");
                writer2.flush();

                writer2.write("position fen " + fen + "\n");
                writer2.flush();

                writer2.write("go depth 18\n");
                writer2.flush();

                String line;
                String[] topMoves = new String[3];
                String[] moveEvaluations = new String[3];
                int[] seenMoves = {0};

                while ((line = reader2.readLine()) != null) {
                    if (line.startsWith("info depth ")) {
                        String[] parts = line.split(" ");
                        int multiPVIndex = Arrays.asList(parts).indexOf("multipv");
                        if (multiPVIndex != -1) {
                            int pv = Integer.parseInt(parts[multiPVIndex + 1]) - 1;
                            if (pv < 3) { // Ci sono al massimo 3 varianti
                                int moveIndex = Arrays.asList(parts).indexOf("pv") + 1;
                                int scoreIndex = Arrays.asList(parts).indexOf("score");
                                String moveInUci = parts[moveIndex];
                                String scoreType = parts[scoreIndex + 1];
                                String moveEvaluation;

                                if (scoreType.equals("cp")) {
                                    double score = Integer.parseInt(parts[scoreIndex + 2]) / 100.0;
                                    double adjustedScore = fen.contains(" b ") ? -score : score;
                                    moveEvaluation = adjustedScore + " CP";
                                } else if (scoreType.equals("mate")) {
                                    int mateIn = Integer.parseInt(parts[scoreIndex + 2]);
                                    moveEvaluation = mateIn > 0 ? "#" + mateIn : "#-" + Math.abs(mateIn);
                                } else {
                                    moveEvaluation = "N/A";
                                }

                                String algebraicMove = ChessMoveConverter.convertToAlgebraicNotation(moveInUci, fen);

                                topMoves[pv] = algebraicMove;
                                moveEvaluations[pv] = moveEvaluation;
                                seenMoves[0]++;
                            }
                        }
                    } else if (line.startsWith("bestmove") || seenMoves[0] == 3) {
                        break;
                    }
                }

                Platform.runLater(() -> {
                    if (seenMoves[0] > 0 && topMoves[0] != null) move1Label.setText(topMoves[0] + " (" + moveEvaluations[0] + ")");
                    if (seenMoves[0] > 1 && topMoves[1] != null) move2Label.setText(topMoves[1] + " (" + moveEvaluations[1] + ")");
                    else move2Label.setText("");
                    if (seenMoves[0] > 2 && topMoves[2] != null) move3Label.setText(topMoves[2] + " (" + moveEvaluations[2] + ")");
                    else move3Label.setText("");
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    public void close() {
        try {
            if (process1 != null) {
                writer1.write("quit\n");
                writer1.flush();
                writer1.close();
                reader1.close();
                process1.destroy();
            }
            if (process2 != null) {
                writer2.write("quit\n");
                writer2.flush();
                writer2.close();
                reader2.close();
                process2.destroy();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
