package org.example.javachess.Oggetti;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveException;
import javafx.application.Platform;
import javafx.scene.control.Label;
import java.io.*;
import java.util.*;

import javafx.scene.paint.Color;
import org.example.javachess.Utils.ChessMoveConverter;

public class Stockfish {

    private Process process1; // Per la valutazione della posizione
    private Process process3; // Per ottenere le prime tre mosse migliori
    private BufferedReader reader1;
    private BufferedReader reader3;
    private OutputStreamWriter writer1;
    private OutputStreamWriter writer3;
    private ChessMoveConverter ChessMoveConverter = new ChessMoveConverter();

    // Aggiungi questa variabile per il livello di abilità
    public Stockfish(String stockfishPath) {
        try {
            String line;
            ProcessBuilder pb1 = new ProcessBuilder(stockfishPath);
            process1 = pb1.start();
            reader1 = new BufferedReader(new InputStreamReader(process1.getInputStream()));
            writer1 = new OutputStreamWriter(process1.getOutputStream());

            writer1.write("uci\n");
            writer1.flush();

            while ((line = reader1.readLine()) != null) {
                System.out.println("Stockfish output: " + line); // Print each line for debugging
                if (line.equals("uciok")) {
                    break;
                }
            }
            ProcessBuilder pb3 = new ProcessBuilder(stockfishPath);
            process3 = pb3.start();
            reader3 = new BufferedReader(new InputStreamReader(process3.getInputStream()));
            writer3 = new OutputStreamWriter(process3.getOutputStream());

            writer3.write("uci\n");
            writer3.flush();

            while ((line = reader3.readLine()) != null) {
                System.out.println("Stockfish output: " + line); // Print each line for debugging
                if (line.equals("uciok")) {
                    break;
                }
            }

            // Set the number of threads
            writer3.write("setoption name Threads value 4\n");
            writer3.flush();
            System.out.println("Set Stockfish to use 1 threads.");

            // Set the hash size (e.g., 64 MB)
            writer3.write("setoption name Hash value 512\n");
            writer3.flush();
            System.out.println("Set Stockfish hash value to 128 MB.");

            // Confirm options are set
            writer3.write("isready\n");
            writer3.flush();

            while ((line = reader3.readLine()) != null) {
                System.out.println("Stockfish output: " + line); // Print confirmation output
                if (line.equals("readyok")) {
                    System.out.println("Stockfish is ready.");
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    // Funzione per impostare il livello di abilità
    public void setSkillLevel(int skillLevel) {
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

            writer1.write("go depth 10\n");
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
    // Funzione per valutare la posizione


    Color LIGHT_ORANGE = Color.rgb(242, 198, 126);
    Color LIGHT_GREEN = Color.rgb(175, 199, 119);





        // ... (altri metodi esistenti)

    public Map<Square, Double> evaluateMovesForPiece(Board board, Square pieceSquare, double currentEvaluation) {
        Map<Square, Double> moveEvaluations = new HashMap<>();
        ChessHelper chessHelper = new ChessHelper();

        try {
            List<Square> legalMoves = chessHelper.getLegalMovesForPiece(board, pieceSquare);
            boolean isWhiteToMove = board.getSideToMove() == Side.WHITE;

            for (Square toSquare : legalMoves) {
                Board tempBoard = new Board();
                tempBoard.loadFromFen(board.getFen());
                Move move = new Move(pieceSquare, toSquare);
                tempBoard.doMove(move);

                writer3.write("setoption name MultiPV value 1\n");
                writer3.flush();
                writer3.write("position fen " + tempBoard.getFen() + "\n");
                writer3.flush();

                writer3.write("go depth 8\n");
                writer3.flush();

                String line;
                double evaluation = 0.0;
                boolean isMate = false;

                while ((line = reader3.readLine()) != null) {
                    if (line.startsWith("info")) {
                        System.out.println("Received from Stockfish: " + line);
                        String[] parts = line.split(" ");
                        int scoreIndex = Arrays.asList(parts).indexOf("score");
                        if (scoreIndex != -1) {
                            String scoreType = parts[scoreIndex + 1];
                            if (scoreType.equals("cp")) {
                                evaluation = Integer.parseInt(parts[scoreIndex + 2]) / 100.0;
                            } else if (scoreType.equals("mate")) {
                                int mateIn = Integer.parseInt(parts[scoreIndex + 2]);
                                evaluation = mateIn > 0 ? 100.00 : -100.00;
                                isMate = true;  // Segnala che questa valutazione è un matto
                            }
                        }
                    } else if (line.startsWith("bestmove")) {
                        break;
                    }
                }

                if (isWhiteToMove) {
                    evaluation = -evaluation;
                }

                System.out.println("Evaluation for move " + toSquare + ": " + evaluation);

                double evaluationDifference = calculateEvaluationDifference(currentEvaluation, evaluation, isWhiteToMove);

                System.out.println("Evaluation difference for move " + move + ": " + evaluationDifference);
                moveEvaluations.put(toSquare, evaluationDifference);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return moveEvaluations;
    }




    private double calculateEvaluationDifference(double currentEvaluation, double newEvaluation, boolean isWhiteToMove) {
        double difference;

        if (isWhiteToMove) {
            // Il bianco muove
            if (currentEvaluation >= 0) {
                // Il bianco sta vincendo o la posizione è pari
                difference = newEvaluation - currentEvaluation;
            } else {
                // Il bianco sta perdendo
                difference = newEvaluation - currentEvaluation;
            }
        } else {
            // Il nero muove
            if (currentEvaluation <= 0) {
                // Il nero sta vincendo o la posizione è pari
                difference = newEvaluation - currentEvaluation;
            } else {
                // Il nero sta perdendo
                difference = newEvaluation - currentEvaluation;
            }
        }

        // Gestiamo il caso in cui la differenza è positiva per evitare che venga considerata una mossa buona per l'avversario
        return difference <= 0 ? difference : -difference;
    }



        public void highlightLegalMovesWithEvaluation(Board board, Square pieceSquare, ChessBoardUI chessBoard, double currentEvaluation) {
        Map<Square, Double> evaluations = evaluateMovesForPiece(board, pieceSquare, currentEvaluation);

        for (Map.Entry<Square, Double> entry : evaluations.entrySet()) {
            Square toSquare = entry.getKey();
            double evalDifference = entry.getValue();

            Color color;
            if (evalDifference >= -0.5) {  // Mossa molto vantaggiosa
                color = Color.GREEN;
            } else if (evalDifference < -0.5 && evalDifference > -1) {  // Imprecisione
                color = Color.YELLOW;
            } else if (evalDifference <= -1.0 && evalDifference > -1.5) {  // Errore
                color = Color.ORANGE;
            } else {  // Blunder o mossa molto cattiva
                color = Color.RED;
            }

            int col = toSquare.ordinal() % 8;
            int row = 7 - (toSquare.ordinal() / 8);
            chessBoard.highlightSquare(col, row, color);
        }
    }





    public void getTopThreeMoves(String fen, Label move1Label, Label move2Label, Label move3Label, ChessBoardUI chessBoard, Label evaluationLabel, EvalBar evalBar) {
        new Thread(() -> {
            if (isCalculating) {
                stopCalculating();
                isCalculating = false;
                try {
                    // Aggiungi un ritardo più lungo per garantire che Stockfish si fermi completamente
                    Thread.sleep(100);
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

                // Verifica che Stockfish sia pronto dopo l'invio del FEN
                writer3.write("isready\n");
                writer3.flush();
                String readyLine;
                while ((readyLine = reader3.readLine()) != null) {
                    if (readyLine.trim().equals("readyok")) {
                        break;
                    }
                }

                writer3.write("go depth 18\n");
                writer3.flush();

                String line;
                String[] topMoves = new String[3];
                String[] moveEvaluations = new String[3];
                String[] fullLines = new String[3];

                while ((line = reader3.readLine()) != null) {
                    if (line.startsWith("info depth ")) {
                        processStockfishOutput(line, fen, topMoves, moveEvaluations, fullLines, chessBoard, move1Label, move2Label, move3Label, evaluationLabel, evalBar);
                    } else if (line.startsWith("bestmove")) {
                        System.out.println("Received from Stockfish (if bestmove): " + line);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isCalculating = false;
                System.out.println("calculation completed.");
            }
        }).start();
    }



    public void stopCalculating() {
        try {
            if (process3 != null) {
                System.out.println("Sending stop command to Stockfish...");
                // Send the stop command
                writer3.write("stop\n");
                writer3.flush();
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            isCalculating = false;
            System.out.println("isCalculating set to false.");
        }
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
                Square fromSquare = Square.valueOf(uciMove.substring(0, 2).toUpperCase());
                Square toSquare = Square.valueOf(uciMove.substring(2, 4).toUpperCase());

                Move move = new Move(fromSquare, toSquare);
                board.doMove(move);  // Aggiorna la scacchiera con la mossa

            } catch (IllegalArgumentException | MoveException e) {
                e.printStackTrace();
                System.err.println("Error executing move: " + uciMove + ". Skipping this move.");
                break;  // Abbandona la linea di mosse se c'è un errore
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

    public void close() {
        try {
            if (process1 != null) {
                writer1.write("quit\n");
                writer1.flush();
                writer1.close();
                reader1.close();
                process1.destroy();
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
}
