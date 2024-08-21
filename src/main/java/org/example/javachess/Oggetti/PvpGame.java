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
import org.example.javachess.Oggetti.ChessBoardUI;
import org.example.javachess.Oggetti.ChessTimer;
import org.example.javachess.Oggetti.Stockfish;

import java.util.List;

public class PvpGame {

    private Board board;
    private ChessBoardUI chessBoardUI;
    private Label evaluationLabel;
    private Label move1Label;
    private Label move2Label;
    private Label move3Label;
    private boolean gameRunning;
    private Stockfish stockfish;
    private ChessTimer chessTimer;
    private int increment;
    private boolean isWhiteTurn;
    private Task<Void> moveCalculationTask; // Task to track move calculations

    public PvpGame(ChessBoardUI chessBoardUI, Label evaluationLabel, Label move1Label, Label move2Label, Label move3Label, Label whiteLabel, Label blackLabel, int gameDuration, int increment) {
        this.board = new Board();
        this.chessBoardUI = chessBoardUI;
        this.evaluationLabel = evaluationLabel;
        this.move1Label = move1Label;
        this.move2Label = move2Label;
        this.move3Label = move3Label;
        this.increment = increment;

        this.chessTimer = new ChessTimer(whiteLabel, blackLabel, gameDuration, increment);
    }

    public void startGame() {
        gameRunning = true;
        stockfish = new Stockfish("/opt/homebrew/bin/stockfish");
        isWhiteTurn = true;
        chessTimer.initializetimer();
        move1Label.setText("");
        move2Label.setText("");
        move3Label.setText("");
        Platform.runLater(() -> chessTimer.startWhiteTimer());

        evaluatePositionAndMoves();
    }

    private void evaluatePositionAndMoves() {
        if (!gameRunning) return;

        if (moveCalculationTask != null && moveCalculationTask.isRunning()) {
            moveCalculationTask.cancel();  // Cancel the previous task if it's still running
        }

        moveCalculationTask = new Task<Void>() {
            @Override
            protected Void call() {
                stockfish.evaluatePosition(board.getFen(), evaluationLabel);
                stockfish.getTopThreeMoves(board.getFen(), move1Label, move2Label, move3Label);
                return null;
            }
        };

        new Thread(moveCalculationTask).start(); // Start the task in a new thread
    }

    public void handleMoveInput(String moveInput) {
        if (!gameRunning) return;

        try {
            Move move = parseMoveInput(moveInput);

            if (move != null && MoveGenerator.generateLegalMoves(board).contains(move)) {
                board.doMove(move);
                Platform.runLater(() -> {
                    chessBoardUI.setPosition(board.getFen());

                    if (isWhiteTurn) {
                        chessTimer.addIncrementToWhite();
                        chessTimer.stopWhiteTimer();
                        chessTimer.startBlackTimer();
                    } else {
                        chessTimer.addIncrementToBlack();
                        chessTimer.stopBlackTimer();
                        chessTimer.startWhiteTimer();
                    }

                    isWhiteTurn = !isWhiteTurn;
                });

                if (board.isMated() || board.isDraw()) {
                    System.out.println("Scaccomatto o partita patta, fine del gioco.");
                    move1Label.setText("scaccomatto o patta");
                    move2Label.setText("");
                    move3Label.setText("");
                    endGame();
                } else {
                    evaluatePositionAndMoves();
                }
            } else {
                System.out.println("Mossa illegale o non valida, riprova.");
            }
        } catch (Exception e) {
            System.out.println("Errore: Mossa non valida. Riprova.");
        }
    }

    public void endGame() {
        gameRunning = false;

        chessTimer.stopWhiteTimer();
        chessTimer.stopBlackTimer();

        if (stockfish != null) {
            stockfish.close();
            stockfish = null;
            System.out.println("Stockfish chiuso.");
        }

        if (moveCalculationTask != null && moveCalculationTask.isRunning()) {
            moveCalculationTask.cancel();  // Cancel any ongoing calculation
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
