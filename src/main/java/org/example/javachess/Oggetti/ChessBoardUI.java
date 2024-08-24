package org.example.javachess.Oggetti;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import org.example.javachess.Controllers.GlobalController;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ChessBoardUI extends GridPane {

    private static final int TILE_SIZE = 60;
    private static final int BOARD_SIZE = 8;
    private Board chessBoard;
    private List<String> moveList;
    private int currentMoveIndex;

    public ChessBoardUI() {
        this.chessBoard = new Board();
        this.setAlignment(Pos.CENTER); // Centra il contenuto del GridPane
        createBoard("marghiacciato.png");
        updateBoard(chessBoard, null); // Passa null alla prima chiamata perché non c'è nessuna mossa precedente
        currentMoveIndex = 0; // Inizializza l'indice della mossa corrente
    }

    private void createBoard(String chessboardStyle) {
        // Carica e ridimensiona l'immagine della scacchiera
        try (InputStream chessBoardImageStream = GlobalController.class.getResourceAsStream("/images/" + chessboardStyle)) {
            if (chessBoardImageStream == null) {
                throw new IllegalArgumentException("Immagine non trovata: /images/" + chessboardStyle);
            }
            Image chessBoardImage = new Image(
                    chessBoardImageStream,
                    TILE_SIZE * BOARD_SIZE,
                    TILE_SIZE * BOARD_SIZE,
                    false,
                    true
            );
            ImageView boardImageView = new ImageView(chessBoardImage);

            // Posiziona l'immagine della scacchiera come sfondo
            this.add(boardImageView, 0, 0, BOARD_SIZE, BOARD_SIZE);

            // Imposta le dimensioni preferite del GridPane in base alla scacchiera
            this.setPrefSize(TILE_SIZE * BOARD_SIZE, TILE_SIZE * BOARD_SIZE);

            // Imposta le costrizioni sulle righe e colonne per mantenere le dimensioni costanti
            for (int i = 0; i < BOARD_SIZE; i++) {
                getColumnConstraints().add(new ColumnConstraints(TILE_SIZE));
                getRowConstraints().add(new RowConstraints(TILE_SIZE));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Node highlightSquare(int col, int row) {
        Rectangle highlight = new Rectangle(TILE_SIZE, TILE_SIZE);
        highlight.setFill(Color.TRANSPARENT); // Transparent fill color
        highlight.setStroke(Color.YELLOW); // Border color
        highlight.setStrokeWidth(3); // Border thickness

        // Apply a blur effect as an example
        GaussianBlur blur = new GaussianBlur(30);
        highlight.setEffect(blur);

        // Position the highlighter on the desired square
        GridPane.setConstraints(highlight, col, row);
        GridPane.setHalignment(highlight, HPos.CENTER);
        GridPane.setValignment(highlight, VPos.CENTER);

        this.add(highlight, col, row);
        return highlight;
    }

    public void resetBoard() {
        chessBoard = new Board();  // Reinizializza la scacchiera alla posizione iniziale
        updateBoard(chessBoard, null);   // Aggiorna la UI senza evidenziare nessuna mossa
        currentMoveIndex = 0;      // Reset dell'indice della mossa corrente
    }

    private Node previousFromHighlight;
    private Node previousToHighlight;

    public void updateBoard(Board board, Move lastMove) {
        // Remove previous highlights
        if (previousFromHighlight != null) {
            this.getChildren().remove(previousFromHighlight);
            previousFromHighlight = null;
        }
        if (previousToHighlight != null) {
            this.getChildren().remove(previousToHighlight);
            previousToHighlight = null;
        }

        if (lastMove != null) {
            Square fromSquare = lastMove.getFrom();
            Square toSquare = lastMove.getTo();

            // Calculate the coordinates of the move
            int fromCol = fromSquare.ordinal() % 8;
            int fromRow = 7 - (fromSquare.ordinal() / 8);
            int toCol = toSquare.ordinal() % 8;
            int toRow = 7 - (toSquare.ordinal() / 8);

            // Highlight the starting square
            previousFromHighlight = highlightSquare(fromCol, fromRow);
            // Highlight the destination square
            previousToHighlight = highlightSquare(toCol, toRow);
        }

        // Update the board pieces
        this.getChildren().removeIf(node -> node instanceof ImageView && node != this.getChildren().get(0));

        for (Square square : Square.values()) {
            Piece piece = board.getPiece(square);
            if (piece != Piece.NONE) {
                String pieceFileName = getPieceFileName(piece);
                try (InputStream pieceImageStream = GlobalController.class.getResourceAsStream("/images/" + pieceFileName)) {
                    if (pieceImageStream == null) {
                        throw new IllegalArgumentException("Image not found: /images/" + pieceFileName);
                    }
                    ImageView pieceImageView = new ImageView(new Image(pieceImageStream));
                    pieceImageView.setFitHeight(TILE_SIZE);
                    pieceImageView.setFitWidth(TILE_SIZE);

                    int col = square.ordinal() % 8;
                    int row = 7 - (square.ordinal() / 8);

                    GridPane.setConstraints(pieceImageView, col, row);
                    GridPane.setHalignment(pieceImageView, HPos.CENTER);
                    GridPane.setValignment(pieceImageView, VPos.CENTER);

                    this.add(pieceImageView, col, row);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

    public void setPosition(String fen, Move lastMove) {
        chessBoard.loadFromFen(fen);
        updateBoard(chessBoard, lastMove);
    }

    public String getFen() {
        return chessBoard.getFen();
    }

    public void loadPgn(String pgn) {
        moveList = parsePgnMoves(pgn);
        resetBoard();
    }

    private List<String> parsePgnMoves(String pgn) {
        List<String> moves = new ArrayList<>();
        String[] tokens = pgn.split("\\s+");

        for (String token : tokens) {
            if (!token.matches("\\d+\\.") && !token.matches("1-0|0-1|1/2-1/2")) {
                moves.add(token);
            }
        }
        return moves;
    }

    public void nextMove() {
        if (moveList != null && currentMoveIndex < moveList.size()) {
            String moveStr = moveList.get(currentMoveIndex);
            Move move = parseMoveFromString(moveStr);
            if (move != null) {
                chessBoard.doMove(move);
                updateBoard(chessBoard, move);
                currentMoveIndex++;
            }
        }
    }

    public void previousMove() {
        if (moveList != null && currentMoveIndex > 0) {
            chessBoard.undoMove();
            currentMoveIndex--;
            updateBoard(chessBoard, null); // Nessun highlight per la mossa precedente
        }
    }

    private Move parseMoveFromString(String moveStr) {
        if (moveStr.length() == 4) {
            Square from = Square.valueOf(moveStr.substring(0, 2).toUpperCase());
            Square to = Square.valueOf(moveStr.substring(2, 4).toUpperCase());
            return new Move(from, to);
        }
        return null;
    }
}
