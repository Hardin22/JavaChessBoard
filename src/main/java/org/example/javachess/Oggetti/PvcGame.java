package org.example.javachess.Oggetti;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;

import java.util.List;

public class PvcGame {

    private Board board;
    private ChessBoardUI chessBoardUI;
    private Label evaluationLabel;
    private Label move1Label;
    private Label move2Label;
    private Label move3Label;
    private boolean gameRunning;
    private Stockfish stockfish;
    private Stockfish playerStockfish; // Stockfish utilizzato per il computer
    private boolean isPlayerWhite;
    private Task<Void> moveCalculationTask; // Task per il calcolo delle mosse

    public PvcGame(ChessBoardUI chessBoardUI, Label evaluationLabel, Label move1Label, Label move2Label, Label move3Label, boolean isPlayerWhite, int skillLevel) {
        this.board = new Board();
        this.chessBoardUI = chessBoardUI;
        this.evaluationLabel = evaluationLabel;
        this.move1Label = move1Label;
        this.move2Label = move2Label;
        this.move3Label = move3Label;
        this.isPlayerWhite = isPlayerWhite;

        // Inizializza Stockfish per l'analisi e per il giocatore/computer
        this.stockfish = new Stockfish("/opt/homebrew/bin/stockfish");
        this.playerStockfish = new Stockfish("/opt/homebrew/bin/stockfish");
        this.playerStockfish.setSkillLevel(skillLevel);
    }

    public void startGame() {
        gameRunning = true;
        move1Label.setText("");
        move2Label.setText("");
        move3Label.setText("");

        Platform.runLater(() -> chessBoardUI.setPosition(board.getFen()));
        evaluatePositionAndMoves();

        if (!isPlayerWhite) {
            handleComputerMove();
        }
    }

    private void evaluatePositionAndMoves() {
        if (!gameRunning) return;

        if (moveCalculationTask != null && moveCalculationTask.isRunning()) {
            moveCalculationTask.cancel();  // Annulla il task precedente se è ancora in esecuzione
        }

        moveCalculationTask = new Task<Void>() {
            @Override
            protected Void call() {
                stockfish.evaluatePosition(board.getFen(), evaluationLabel);
                stockfish.getTopThreeMoves(board.getFen(), move1Label, move2Label, move3Label);
                return null;
            }
        };

        new Thread(moveCalculationTask).start(); // Avvia il task in un nuovo thread
    }

    public void handleMoveInput(String moveInput) {
        if (!gameRunning) return;

        try {
            Move move = parseMoveInput(moveInput);

            if (move != null && MoveGenerator.generateLegalMoves(board).contains(move)) {
                board.doMove(move);
                Platform.runLater(() -> chessBoardUI.setPosition(board.getFen()));

                if (board.isMated() || board.isDraw()) {
                    endGameWithMessage("Scaccomatto o partita patta, fine del gioco.");
                } else {
                    handleComputerMove();
                }
            } else {
                System.out.println("Mossa illegale o non valida, riprova.");
            }
        } catch (Exception e) {
            System.out.println("Errore: Mossa non valida. Riprova.");
        }
    }

    private void handleComputerMove() {
        if (!gameRunning) return;

        Task<Void> computerMoveTask = new Task<Void>() {
            @Override
            protected Void call() {
                String bestMoveUci = playerStockfish.getBestMove(board.getFen());
                if (bestMoveUci != null) {
                    Move bestMove = parseMoveUci(bestMoveUci);
                    board.doMove(bestMove);

                    // Aggiorna la scacchiera e calcola le nuove mosse
                    Platform.runLater(() -> {
                        chessBoardUI.setPosition(board.getFen());

                        if (board.isMated() || board.isDraw()) {
                            endGameWithMessage("Scaccomatto o partita patta, fine del gioco.");
                        } else {
                            // Dopo la mossa del computer, aggiorna le migliori tre mosse per la posizione corrente
                            evaluatePositionAndMoves();
                        }
                    });
                }
                return null;
            }
        };

        new Thread(computerMoveTask).start(); // Avvia il task della mossa del computer
    }


    private void endGameWithMessage(String message) {
        System.out.println(message);
        evaluationLabel.setText("");
        move1Label.setText(message);
        move2Label.setText("");
        move3Label.setText("");
        endGame();
    }

    public void endGame() {
        gameRunning = false;

        if (stockfish != null) {
            stockfish.close();
            stockfish = null;
            System.out.println("Stockfish (analisi) chiuso.");
        }

        if (playerStockfish != null) {
            playerStockfish.close();
            playerStockfish = null;
            System.out.println("Stockfish (giocatore) chiuso.");
        }

        if (moveCalculationTask != null && moveCalculationTask.isRunning()) {
            moveCalculationTask.cancel();  // Annulla qualsiasi calcolo in corso
        }
    }

    private Move parseMoveInput(String moveInput) {
        if (moveInput.length() == 4) {
            Square from = Square.valueOf(moveInput.substring(0, 2).toUpperCase());
            Square to = Square.valueOf(moveInput.substring(2, 4).toUpperCase());
            return new Move(from, to);
        } else if (moveInput.length() == 3) {
            char pieceChar = moveInput.charAt(0);
            Square to = Square.valueOf(moveInput.substring(1, 3).toUpperCase());
            List<Move> legalMoves = MoveGenerator.generateLegalMoves(board);

            for (Move move : legalMoves) {
                if (move.getTo().equals(to) && board.getPiece(move.getFrom()).equals(parsePiece(pieceChar))) {
                    return move;
                }
            }
        } else if (moveInput.length() == 2) {
            Square to = Square.valueOf(moveInput.toUpperCase());
            List<Move> legalMoves = MoveGenerator.generateLegalMoves(board);

            for (Move move : legalMoves) {
                if (move.getTo().equals(to) && board.getPiece(move.getFrom()).equals(parsePiece('P'))) {
                    return move;
                }
            }
        }

        return null;
    }

    private Move parseMoveUci(String uciMove) {
        Square from = Square.valueOf(uciMove.substring(0, 2).toUpperCase());
        Square to = Square.valueOf(uciMove.substring(2, 4).toUpperCase());
        return new Move(from, to);
    }

    private Piece parsePiece(char pieceChar) {
        switch (Character.toUpperCase(pieceChar)) {
            case 'R': return board.getSideToMove() == Side.WHITE ? Piece.WHITE_ROOK : Piece.BLACK_ROOK;
            case 'N': return board.getSideToMove() == Side.WHITE ? Piece.WHITE_KNIGHT : Piece.BLACK_KNIGHT;
            case 'B': return board.getSideToMove() == Side.WHITE ? Piece.WHITE_BISHOP : Piece.BLACK_BISHOP;
            case 'Q': return board.getSideToMove() == Side.WHITE ? Piece.WHITE_QUEEN : Piece.BLACK_QUEEN;
            case 'K': return board.getSideToMove() == Side.WHITE ? Piece.WHITE_KING : Piece.BLACK_KING;
            case 'P': return board.getSideToMove() == Side.WHITE ? Piece.WHITE_PAWN : Piece.BLACK_PAWN;
            default: throw new IllegalArgumentException("Pezzo non valido: " + pieceChar);
        }
    }
}
