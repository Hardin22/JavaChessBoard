package org.example.javachess.Oggetti;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveException;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import org.example.javachess.Oggetti.ChessHelper;
import javafx.scene.text.Font;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import javafx.scene.paint.Color;
import org.example.javachess.Utils.ChessMoveConverter;

public class Stockfish {

    private Process process1; // Per la valutazione della posizione
    private Process process2; // Per il calcolo delle migliori mosse
    private Process process3; // Per ottenere le prime tre mosse migliori
    private BufferedReader reader1;
    private BufferedReader reader2;
    private BufferedReader reader3;
    private OutputStreamWriter writer1;
    private OutputStreamWriter writer2;
    private OutputStreamWriter writer3;
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

            // Inizializza il terzo processo di Stockfish
            ProcessBuilder pb3 = new ProcessBuilder(stockfishPath);
            process3 = pb3.start();
            reader3 = new BufferedReader(new InputStreamReader(process3.getInputStream()));
            writer3 = new OutputStreamWriter(process3.getOutputStream());

            writer3.write("uci\n");
            writer3.flush();

            while ((line = reader3.readLine()) != null) {
                if (line.equals("uciok")) {
                    break;
                }
            }
            writer3.write("setoption name Threads value 3\n");
            writer3.flush();

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

    private volatile boolean isCalculating = false;
    private volatile boolean isEvaluating = false;
    // Funzione per valutare la posizione
    public void evaluatePoition(String fen, Label evaluationLabel, EvalBar evalBar) {

        isEvaluating = true;
        new Thread(() -> {
            try {
                writer1.write("position fen " + fen + "\n");
                writer1.flush();

                writer1.write("go depth 20\n");
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
            } finally {
                isEvaluating = false;
                System.out.println("valutazinoe finita.");
            }
        }).start();
    }

    Color LIGHT_ORANGE = Color.rgb(242, 198, 126);
    Color LIGHT_GREEN = Color.rgb(175, 199, 119);



    private void stopCalculating(OutputStreamWriter writer, BufferedReader reader, Process process) {
        try {
            if (process != null) {
                System.out.println("Sending stop command to Stockfish...");
                writer.write("stop\n");
                writer.flush();

                // Wait for confirmation from Stockfish
                String line;
                boolean stopped = false;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Received from Stockfish: " + line);
                    if (line.startsWith("info") && line.contains("nodes")) {
                        stopped = true;
                        System.out.println("Stockfish has stopped calculating.");
                        break;
                    }
                }
                if (!stopped) {
                    System.out.println("Did not receive stop confirmation, waiting a bit longer...");
                    Thread.sleep(100);  // Aspetta un po' e riprova
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("isCalculating set to false.");
    }

    /*public void getTopThreeMoves(String fen, Label move1Label, Label move2Label, Label move3Label, ChessBoardUI chessBoard) {
        new Thread(() -> {
            if (isCalculating) {
                stopCalculating(writer3, reader3, process3);
                isCalculating = false;
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            isCalculating = true;
            try {
                writer3.write("setoption name MultiPV value 3\n");
                writer3.flush();

                writer3.write("position fen " + fen + "\n");
                writer3.flush();

                writer3.write("go depth 22\n");
                writer3.flush();

                String line;
                String[] topMoves = new String[3];
                String[] moveEvaluations = new String[3];

                while ((line = reader3.readLine()) != null) {
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
            }finally {
                isCalculating = false;
                System.out.println("calcoli finiti");
            }
        }).start();
    }*/



        // ... (altri metodi esistenti)
    public Map<Square, Double> evaluateMovesForPiece(Board board, Square pieceSquare, double currentEvaluation) {
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
                boolean isMate = false;

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
                                evaluation = mateIn > 0 ? 100.0 : -100.0;  // Usa 100 per rappresentare il matto
                                isMate = true; // Indica che si tratta di un matto
                            }
                        }
                    } else if (line.startsWith("bestmove")) {
                        break;
                    }
                }

                System.out.println("Evaluation for " + toSquare + ": " + evaluation);

                // Calcola la differenza tra la valutazione attuale e quella della nuova posizione
                double evaluationDifference;
                if (isMate) {
                    // Per i matti, usa la differenza diretta
                    evaluationDifference =currentEvaluation-evaluation;
                } else {
                    // Negazione della valutazione per confrontarla correttamente
                    evaluationDifference = currentEvaluation - evaluation;
                }

                System.out.println("Evaluation difference: " + evaluationDifference);
                moveEvaluations.put(toSquare, isMate && evaluation == 0.0 ? 0.00 : evaluationDifference);                }
        } catch (Exception e) {
            e.printStackTrace();
        }
            return moveEvaluations;
        }
        //UTILIZZA I VALORI ASSOLUTIIIIIII PORCODIO


    // Nuova funzione per valutare le mosse legali di un pezzo

    public void highlightLegalMovesWithEvaluation(Board board, Square pieceSquare, ChessBoardUI chessBoard, double currentEvaluation) {
        Map<Square, Double> evaluations = evaluateMovesForPiece(board, pieceSquare, currentEvaluation);

        for (Map.Entry<Square, Double> entry : evaluations.entrySet()) {
            Square toSquare = entry.getKey();
            double evalDifference = entry.getValue();

            Color color;
            if (evalDifference > -0.5) {
                color = Color.GREEN; // Buona mossa
            } else if (evalDifference < 0.5 && evalDifference > -1.0) {
                color = Color.YELLOW; // Mossa neutra o imprecisione
            } else if (evalDifference < -1.0 && evalDifference > -2.0) {
                color = Color.ORANGE; // Errore o mossa sbagliata
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
            if (process3 != null) {
                writer3.write("quit\n");
                writer3.flush();
                writer3.close();
                reader3.close();
                process3.destroy();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //test per le linee principali//


    public void getTopThreeMoves(String fen, Label move1Label, Label move2Label, Label move3Label, ChessBoardUI chessBoard, Label evaluationLabel, EvalBar evalBar) {
        new Thread(() -> {
            if (isCalculating) {
                stopCalculating(writer3, reader3, process3);
                isCalculating = false;
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            isCalculating = true;
            try {
                writer3.write("setoption name MultiPV value 3\n");
                writer3.flush();

                writer3.write("position fen " + fen + "\n");
                writer3.flush();

                writer3.write("go depth 18\n");
                writer3.flush();

                String line;
                String[] topMoves = new String[3];
                String[] moveEvaluations = new String[3]; // Array per memorizzare le valutazioni
                String[] fullLines = new String[3]; // Array per memorizzare le linee di mosse complete

                while ((line = reader3.readLine()) != null) {
                    if (line.startsWith("info depth ")) {
                        processStockfishOutput(line, fen, topMoves, moveEvaluations, fullLines, chessBoard, move1Label, move2Label, move3Label, evaluationLabel, evalBar);
                    } else if (line.startsWith("bestmove")) {
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isCalculating = false;
                System.out.println("calcoli finiti");
            }
        }).start();
    }

    private void processStockfishOutput(String line, String fen, String[] topMoves, String[] moveEvaluations, String[] fullLines,
                                        ChessBoardUI chessBoard, Label move1Label, Label move2Label, Label move3Label, Label evaluationLabel, EvalBar evalBar) {
        String[] parts = line.split(" ");
        int multiPVIndex = Arrays.asList(parts).indexOf("multipv");
        if (multiPVIndex != -1) {
            int pv = Integer.parseInt(parts[multiPVIndex + 1]) - 1;
            if (pv < 3) {
                int pvIndex = Arrays.asList(parts).indexOf("pv");
                if (pvIndex == -1 || pvIndex + 1 >= parts.length) {
                    return; // Linea PV malformata
                }
                int moveIndex = pvIndex + 1;
                int scoreIndex = Arrays.asList(parts).indexOf("score");

                if (scoreIndex == -1 || scoreIndex + 2 >= parts.length) {
                    return; // Linea score malformata
                }

                double adjustedScore = calculateAdjustedScore(parts, scoreIndex, fen);
                String moveEvaluation = formatMoveEvaluation(parts, scoreIndex, adjustedScore);
                fullLines[pv] = buildFullLine(moveIndex, parts, fen, moveEvaluation);

                topMoves[pv] = parts[moveIndex];
                moveEvaluations[pv] = moveEvaluation;

                updateUI(pv, topMoves[pv], fullLines[pv], adjustedScore, chessBoard, move1Label, move2Label, move3Label, evaluationLabel, evalBar, moveEvaluations);
            }
        }
    }

    private double calculateAdjustedScore(String[] parts, int scoreIndex, String fen) {
        String scoreType = parts[scoreIndex + 1];
        String[] fenParts = fen.split(" ");
        String sideToMove = fenParts.length > 1 ? fenParts[1] : "w"; // Default a "w" se il parsing fallisce

        if (scoreType.equals("cp")) {
            double score = Double.parseDouble(parts[scoreIndex + 2].replace(",", ".")) / 100.0;
            return sideToMove.equals("b") ? -score : score;
        } else if (scoreType.equals("mate")) {
            int mateIn = Integer.parseInt(parts[scoreIndex + 2]);

            if (mateIn > 0) {
                if (sideToMove.equals("w")) {
                    // Bianco può fare scacco matto in mateIn mosse
                    return mateIn;
                } else {
                    // Nero può fare scacco matto in mateIn mosse, quindi è negativo per bianco
                    return -mateIn;
                }
            } else { // mateIn < 0
                if (sideToMove.equals("w")) {
                    // Bianco sarà messo sotto scacco matto in abs(mateIn) mosse, quindi negativo
                    return mateIn;
                } else {
                    // Nero sarà messo sotto scacco matto in abs(mateIn) mosse, quindi positivo per bianco
                    return -mateIn;
                }
            }
        }
        return 0.0; // Valore di default in caso di errore
    }

    private String formatMoveEvaluation(String[] parts, int scoreIndex, double adjustedScore) {
        String scoreType = parts[scoreIndex + 1];
        if (scoreType.equals("cp")) {
            return String.format("%.2f", adjustedScore);
        } else if (scoreType.equals("mate")) {
            int mateIn = (int) Math.abs(adjustedScore);
            if (adjustedScore > 0) {
                return "#" + mateIn;
            } else {
                return "#-" + mateIn;
            }
        }
        return "N/A";
    }

    private String buildFullLine(int moveIndex, String[] parts, String fen, String moveEvaluation) {
        StringBuilder fullLine = new StringBuilder();
        fullLine.append("[").append(moveEvaluation).append("] ");
        int moveNumber = 1;
        boolean isWhiteMove = true;

        Board board = new Board();
        board.loadFromFen(fen);  // Carica il FEN iniziale

        for (int i = moveIndex; i < parts.length; i++) {
            if (isWhiteMove) {
                fullLine.append(moveNumber).append(")");
                moveNumber++;
            }

            String uciMove = parts[i];
            String algebraicMove = ChessMoveConverter.convertToAlgebraicNotation(uciMove, board.getFen());
            fullLine.append(algebraicMove).append(" ");

            // Esegui la mossa sulla scacchiera per aggiornare il FEN
            try {
                Move move = new Move(Square.valueOf(uciMove.substring(0, 2).toUpperCase()),
                        Square.valueOf(uciMove.substring(2, 4).toUpperCase()));
                board.doMove(move);
            } catch (MoveException e) {
                e.printStackTrace();
            }

            isWhiteMove = !isWhiteMove;
        }
        return fullLine.toString().trim();
    }


    private void updateUI(int pv, String move, String fullLine, double adjustedScore, ChessBoardUI chessBoard, Label move1Label, Label move2Label, Label move3Label, Label evaluationLabel, EvalBar evalBar, String[] moveEvaluations) {
        Platform.runLater(() -> {
            if (pv == 0) {
                chessBoard.clearArrows();
            }

            switch (pv) {
                case 0 -> {
                    move1Label.setText(fullLine);
                    drawArrowFromMove(move, chessBoard, LIGHT_GREEN);
                }
                case 1 -> {
                    move2Label.setText(fullLine);
                    drawArrowFromMove(move, chessBoard, LIGHT_ORANGE);
                }
                case 2 -> {
                    move3Label.setText(fullLine);
                    drawArrowFromMove(move, chessBoard, LIGHT_ORANGE);
                }
            }

            if (pv == 0) {
                evaluationLabel.setText("Evaluation: " + moveEvaluations[0]);
                if (moveEvaluations[0].startsWith("#")) {
                    // Se si tratta di uno scacco matto, passiamo un valore estremo per indicarlo
                    int mateIn = Integer.parseInt(moveEvaluations[0].replace("#", "").replace("-", ""));
                    double evalScore = moveEvaluations[0].contains("-") ? -1000.0 + mateIn : 1000.0 - mateIn;
                    evalBar.updateEvaluation(evalScore);
                } else {
                    evalBar.updateEvaluation(adjustedScore);
                }
            }
        });
    }


}
