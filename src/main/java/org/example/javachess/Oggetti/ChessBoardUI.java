package org.example.javachess.Oggetti;


import javafx.scene.layout.GridPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Square;

public class ChessBoardUI extends GridPane {

    private static final int TILE_SIZE = 64;
    private static final int BOARD_SIZE = 8;
    private Board chessBoard;

    public ChessBoardUI() {
        this.chessBoard = new Board();
        createBoard();
        updateBoard(chessBoard);
    }

    private void createBoard() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Rectangle square = new Rectangle(TILE_SIZE, TILE_SIZE);
                if ((row + col) % 2 == 0) {
                    square.setFill(Color.WHITE);
                } else {
                    square.setFill(Color.FORESTGREEN);
                }
                this.add(square, col, row);
            }
        }
    }
    public void resetBoard() {
        chessBoard = new Board();  // Reinizializza la scacchiera alla posizione iniziale
        updateBoard(chessBoard);   // Aggiorna la UI
    }

    public void updateBoard(Board board) {
        this.getChildren().removeIf(node -> node instanceof ImageView);

        for (Square square : Square.values()) {
            Piece piece = board.getPiece(square);
            if (piece != Piece.NONE) {
                String pieceFileName = getPieceFileName(piece);
                ImageView pieceImageView = new ImageView(new Image(getClass().getResourceAsStream("/images/" + pieceFileName)));
                pieceImageView.setFitHeight(TILE_SIZE);
                pieceImageView.setFitWidth(TILE_SIZE);

                int col = square.ordinal() % 8;
                int row = 7 - (square.ordinal() / 8);

                this.add(pieceImageView, col, row);
            }
        }
    }

    private String getPieceFileName(Piece piece) {
        switch (piece) {
            case WHITE_PAWN: return "wP.png";
            case WHITE_ROOK: return "wR.png";
            case WHITE_KNIGHT: return "wN.png";
            case WHITE_BISHOP: return "wB.png";
            case WHITE_QUEEN: return "wQ.png";
            case WHITE_KING: return "wK.png";
            case BLACK_PAWN: return "p.png";
            case BLACK_ROOK: return "r.png";
            case BLACK_KNIGHT: return "n.png";
            case BLACK_BISHOP: return "b.png";
            case BLACK_QUEEN: return "q.png";
            case BLACK_KING: return "k.png";
            default: return null;
        }
    }

    public void setPosition(String fen) {
        chessBoard.loadFromFen(fen);
        updateBoard(chessBoard);
    }
}
