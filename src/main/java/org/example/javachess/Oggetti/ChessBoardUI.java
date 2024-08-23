package org.example.javachess.Oggetti;


import javafx.geometry.Pos;
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
        this.setAlignment(Pos.CENTER); // Centra il conte scnuto del GridPane
        createBoard();
        updateBoard(chessBoard);
    }

    private void createBoard() {
        Color lightWood = Color.rgb(125, 152, 172); // Light wood color
        Color darkWood = Color.rgb(215, 224, 229);  // Dark wood color

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Rectangle square = new Rectangle(TILE_SIZE, TILE_SIZE);
                if ((row + col) % 2 == 0) {
                    square.setFill(lightWood);
                } else {
                    square.setFill(darkWood);
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
            case WHITE_PAWN: return "wp.png";
            case WHITE_ROOK: return "wr.png";
            case WHITE_KNIGHT: return "wn.png";
            case WHITE_BISHOP: return "wb.png";
            case WHITE_QUEEN: return "wq.png";
            case WHITE_KING: return "wk.png";
            case BLACK_PAWN: return "bp.png";
            case BLACK_ROOK: return "br.png";
            case BLACK_KNIGHT: return "bn.png";
            case BLACK_BISHOP: return "bb.png";
            case BLACK_QUEEN: return "bq.png";
            case BLACK_KING: return "bk.png";
            default: return null;
        }
    }

    public void setPosition(String fen) {
        chessBoard.loadFromFen(fen);
        updateBoard(chessBoard);
    }
}
