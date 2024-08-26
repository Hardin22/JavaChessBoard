package org.example.javachess.Oggetti;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import javafx.application.Platform;
import javafx.scene.control.Label;
import org.example.javachess.Oggetti.ChessHelper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.paint.Color;
import org.example.javachess.Utils.ChessMoveConverter;

public class Stockfish {

    private Process process1; // Per la valutazione della posizione
    private Process process2; // Per il calcolo delle migliori mosse
    private BufferedReader reader1;
    private BufferedReader reader2;
    private OutputStreamWriter writer1;
    private OutputStreamWriter writer2;
    private ChessMoveConverter ChessMoveConverter = new ChessMoveConverter();

    // Aggiungi questa variabile per il livello di abilità
    private int skillLevel;
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

            // Configura Stockfish per utilizzare 4 thread
            writer1.write("setoption name Threads value 2\n");
            writer1.flush();

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

            // Configura il secondo processo per utilizzare 4 thread
            writer2.write("setoption name Threads value 2\n");
            writer2.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Funzione per impostare il livello di abilità
    public void setSkillLevel(int skillLevel) {
        this.skillLevel = skillLevel;
        try {
            writer1.write("setoption name Skill Level value " + skillLevel + "\n");
            writer1.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Funzione per ottenere la migliore mossa a seconda del livello di abilità
    public String getBestMove(String fen) {
        try {
            writer1.write("position fen " + fen + "\n");
            writer1.flush();

            writer1.write("go depth 14\n");
            writer1.flush();

            String line;
            while ((line = reader1.readLine()) != null) {
                if (line.startsWith("bestmove")) {
                    return line.split(" ")[1];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    // Funzione per valutare la posizione
    public void evaluatePosition(String fen, Label evaluationLabel, EvalBar evalBar) {
        new Thread(() -> {
            try {
                writer2.write("position fen " + fen + "\n");
                writer2.flush();

                writer2.write("go depth 14\n");
                writer2.flush();

                String line;
                while ((line = reader2.readLine()) != null) {
                    if (line.startsWith("info depth ")) {
                        String[] parts = line.split(" ");
                        final int depth = Integer.parseInt(parts[2]);
                        final int scoreIndex = Arrays.asList(parts).indexOf("score");

                        if (scoreIndex != -1) {
                            final String scoreType = parts[scoreIndex + 1];

                            if (scoreType.equals("cp")) {
                                final double score = Integer.parseInt(parts[scoreIndex + 2]) / 100.0;
                                final double adjustedScore = fen.contains(" b ") ? -score : score;
                                Platform.runLater(() -> {
                                    evaluationLabel.setText("Depth " + depth + ": " + adjustedScore);
                                    evalBar.updateEvaluation(adjustedScore);
                                });

                            } else if (scoreType.equals("mate")) {
                                final int mateIn = Integer.parseInt(parts[scoreIndex + 2]);
                                final int adjustedMate = fen.contains(" b ") ? -mateIn : mateIn;
                                final String mateText = adjustedMate > 0 ? "#" + adjustedMate : "#-" + Math.abs(adjustedMate);

                                Platform.runLater(() -> {
                                    // Aggiorna correttamente l'etichetta con il segno corretto
                                    evaluationLabel.setText("Depth " + depth + ": " + mateText);
                                    evalBar.updateEvaluation(adjustedMate > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY);
                                });
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

    Color LIGHT_ORANGE = Color.rgb(242, 198, 126);
    Color LIGHT_GREEN = Color.rgb(175, 199, 119);

    public void getTopThreeMoves(String fen, Label move1Label, Label move2Label, Label move3Label, ChessBoardUI chessBoard) {
        new Thread(() -> {
            try {
                writer2.write("setoption name MultiPV value 3\n");
                writer2.flush();

                writer2.write("position fen " + fen + "\n");
                writer2.flush();

                writer2.write("go depth 14\n");
                writer2.flush();

                String line;
                String[] topMoves = new String[3];
                String[] moveEvaluations = new String[3];

                while ((line = reader2.readLine()) != null) {
                    if (line.startsWith("info depth ")) {
                        String[] parts = line.split(" ");
                        int multiPVIndex = Arrays.asList(parts).indexOf("multipv");
                        if (multiPVIndex != -1) {
                            int pv = Integer.parseInt(parts[multiPVIndex + 1]) - 1;
                            if (pv < 3) {
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

                                // Aggiorna la label e disegna la freccia
                                int finalPv = pv;
                                Platform.runLater(() -> {
                                    // Cancella tutte le frecce prima di disegnare le nuove
                                    if (finalPv == 0) {
                                        chessBoard.clearArrows();
                                    }

                                    switch (finalPv) {
                                        case 0 -> {
                                            move1Label.setText(topMoves[0] + " (" + moveEvaluations[0].replace(" CP", "") + ")");
                                            drawArrowFromMove(moveInUci, chessBoard, LIGHT_GREEN);
                                        }
                                        case 1 -> {
                                            move2Label.setText(topMoves[1] + " (" + moveEvaluations[1].replace(" CP", "") + ")");
                                            drawArrowFromMove(moveInUci, chessBoard, LIGHT_ORANGE);
                                        }
                                        case 2 -> {
                                            move3Label.setText(topMoves[2] + " (" + moveEvaluations[2].replace(" CP", "") + ")");
                                            drawArrowFromMove(moveInUci, chessBoard, LIGHT_ORANGE);
                                        }
                                    }
                                });
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

        // ... (altri metodi esistenti)

        // Nuova funzione per valutare le mosse legali di un pezzo
    public Map<Square, Double> evaluateMovesForPiece(Board board, Square pieceSquare) {
        Map<Square, Double> moveEvaluations = new HashMap<>();
        ChessHelper chessHelper = new ChessHelper();

        try {
            // Ottieni tutte le mosse legali per il pezzo selezionato
            List<Square> legalMoves = chessHelper.getLegalMovesForPiece(board, pieceSquare);

            // Valuta ciascuna mossa
            for (Square toSquare : legalMoves) {
                Board tempBoard = new Board();
                tempBoard.loadFromFen(board.getFen());
                Move move = new Move(pieceSquare, toSquare);
                tempBoard.doMove(move);

                // Utilizza il writer1 per valutare rapidamente la posizione
                writer1.write("position fen " + tempBoard.getFen() + "\n");
                writer1.flush();

                writer1.write("go depth 7\n"); // Profondità bassa per velocità
                writer1.flush();

                String line;
                double evaluation = 0.0;
                while ((line = reader1.readLine()) != null) {
                    if (line.startsWith("info depth 7")) {
                        String[] parts = line.split(" ");
                        int scoreIndex = Arrays.asList(parts).indexOf("score");
                        if (scoreIndex != -1) {
                            String scoreType = parts[scoreIndex + 1];
                            if (scoreType.equals("cp")) {
                                evaluation = Integer.parseInt(parts[scoreIndex + 2]) / 100.0;
                            } else if (scoreType.equals("mate")) {
                                int mateIn = Integer.parseInt(parts[scoreIndex + 2]);
                                evaluation = mateIn > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
                            }
                        }
                    } else if (line.startsWith("bestmove")) {
                        break;
                    }
                }

                // Aggiungi la valutazione alla mappa
                moveEvaluations.put(toSquare, evaluation);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return moveEvaluations;
    }
    public void highlightLegalMovesWithEvaluation(Board board, Square pieceSquare, ChessBoardUI chessBoard) {
        Map<Square, Double> evaluations = evaluateMovesForPiece(board, pieceSquare);

        for (Map.Entry<Square, Double> entry : evaluations.entrySet()) {
            Square toSquare = entry.getKey();
            double eval = entry.getValue();

            Color color;
            if (eval > 0.5) {
                color = Color.GREEN; // Buona mossa
            } else if (eval > -0.5) {
                color = Color.YELLOW; // Mossa neutra o imprecisione
            } else {
                color = Color.RED; // Blunder o mossa cattiva
            }

            int col = toSquare.ordinal() % 8;
            int row = 7 - (toSquare.ordinal() / 8);
            chessBoard.highlightSquare(col, row, color);
        }
    }







    // Funzione per convertire la mossa UCI in coordinate e disegnare la freccia
    private void drawArrowFromMove(String moveInUci, ChessBoardUI chessBoard, Color color) {
        // Converti la mossa in coordinate
        int fromCol = moveInUci.charAt(0) - 'a';
        int fromRow = '8' - moveInUci.charAt(1);
        int toCol = moveInUci.charAt(2) - 'a';
        int toRow = '8' - moveInUci.charAt(3);

        // Disegna la freccia sulla scacchiera
        chessBoard.drawArrowOnBoard(fromCol, fromRow, toCol, toRow, color);
    }



    // Funzione per ottenere le prime tre mosse migliori



    // Funzione per chiudere Stockfish
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
