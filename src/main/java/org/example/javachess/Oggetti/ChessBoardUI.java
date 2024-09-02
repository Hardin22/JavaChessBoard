package org.example.javachess.Oggetti;

import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ChessBoardUI extends GridPane {

    private static final int TILE_SIZE = 85;
    private static final int BOARD_SIZE = 8;
    private final String chessboardStyle;
    private final String pieceStyle;
    private Board chessBoard;
    private List<String> moveList;
    private int currentMoveIndex;
    private Canvas arrowCanvas;

    public ChessBoardUI(String chessboardStyle, String piecesStyle) {
        this.chessBoard = new Board();
        this.setAlignment(Pos.CENTER); // Centra il contenuto del GridPane
        this.chessboardStyle = chessboardStyle;
        this.pieceStyle = piecesStyle;
        createBoard(chessboardStyle);
        createArrowCanvas();
        updateBoard(chessBoard, null, pieceStyle); // Passa null alla prima chiamata perché non c'è nessuna mossa precedente
        currentMoveIndex = 0; // Inizializza l'indice della mossa corrente
    }

    private void createBoard(String chessboardStyle) {
        // Carica e ridimensiona l'immagine della scacchiera
        try (InputStream chessBoardImageStream = getClass().getResourceAsStream("/images/Scacchiere/" + chessboardStyle)) {
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

    private void createArrowCanvas() {
        arrowCanvas = new Canvas(TILE_SIZE * BOARD_SIZE, TILE_SIZE * BOARD_SIZE);

        // Posiziona il Canvas esattamente sopra la scacchiera
        GridPane.setConstraints(arrowCanvas, 0, 0, BOARD_SIZE, BOARD_SIZE);
        GridPane.setHalignment(arrowCanvas, HPos.CENTER);
        GridPane.setValignment(arrowCanvas, VPos.CENTER);

        this.getChildren().add(arrowCanvas); // Aggiungi il Canvas sopra la scacchiera

        // Fill the entire canvas with black color for testing
    }

    private void drawArrow(GraphicsContext gc, double startX, double startY, double endX, double endY, Color color) {
        gc.setStroke(color);
        gc.setLineWidth(10); // Spessore della linea

        // Calcola l'angolo della freccia
        double angle = Math.atan2(endY - startY, endX - startX);

        // Lunghezza e larghezza della punta della freccia
        double arrowLength = 25;
        double arrowWidth = 30; // Aumenta questo valore per rendere la freccia più larga

        // Calcola le nuove coordinate del punto finale della barra (ridotto per lasciare spazio alla punta)
        double newEndX = endX - arrowLength * Math.cos(angle);
        double newEndY = endY - arrowLength * Math.sin(angle);

        // Disegna la linea principale della freccia
        gc.strokeLine(startX, startY, newEndX, newEndY);

        // Calcola le coordinate del triangolo (punta della freccia)
        double x1 = endX;
        double y1 = endY;
        double x2 = endX - arrowLength * Math.cos(angle - Math.PI / 5); // Più stretto o più largo
        double y2 = endY - arrowLength * Math.sin(angle - Math.PI / 5);
        double x3 = endX - arrowLength * Math.cos(angle + Math.PI / 5); // Più stretto o più largo
        double y3 = endY - arrowLength * Math.sin(angle + Math.PI / 5);

        // Disegna la punta della freccia come un semplice triangolo
        gc.setFill(color); // Riempie il triangolo con lo stesso colore
        gc.fillPolygon(new double[]{x1, x2, x3}, new double[]{y1, y2, y3}, 3);
    }


    public void drawArrowOnBoard(int fromCol, int fromRow, int toCol, int toRow, Color color) {
        // Calcola il centro esatto del quadrato di partenza e di arrivo
        double startX = (fromCol + 0.5) * TILE_SIZE;
        double startY = (fromRow + 0.5) * TILE_SIZE;
        double endX = (toCol + 0.5) * TILE_SIZE;
        double endY = (toRow + 0.5) * TILE_SIZE;

        // Disegna la freccia
        GraphicsContext gc = arrowCanvas.getGraphicsContext2D();
        drawArrow(gc, startX, startY, endX, endY, color);
    }






    public void resetBoard() {
        chessBoard = new Board();  // Reinizializza la scacchiera alla posizione iniziale
        updateBoard(chessBoard, null, pieceStyle);   // Aggiorna la UI senza evidenziare nessuna mossa
        currentMoveIndex = 0;      // Reset dell'indice della mossa corrente
        clearArrows(); // Pulisce il Canvas delle frecce
    }

    private Node previousFromHighlight;
    private Node previousToHighlight;

    public void updateBoard(Board board, Move lastMove, String pieceStyle) {
        clearHighlights();
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
            previousFromHighlight = highlightSquare(fromCol, fromRow, Color.YELLOW);
            // Highlight the destination square
            previousToHighlight = highlightSquare(toCol, toRow, Color.YELLOW);
        }

        // Update the board pieces
        this.getChildren().removeIf(node -> node instanceof ImageView && node != this.getChildren().get(0));

        for (Square square : Square.values()) {
            Piece piece = board.getPiece(square);
            if (piece != Piece.NONE) {
                String pieceFileName = getPieceFileName(piece);
                try (InputStream pieceImageStream = getClass().getResourceAsStream("/images/Pieces/" + pieceStyle + "/" + pieceFileName)) {
                    if (pieceImageStream == null) {
                        throw new IllegalArgumentException("Image not found: /images/Pezzi/Vetro/" + pieceFileName);
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
    public void clearHighlights() {
        // Rimuovi tutti i nodi di tipo Rectangle (gli highlight) dalla scacchiera
        this.getChildren().removeIf(node -> node instanceof Rectangle);
    }


    public void clearArrows() {
        GraphicsContext gc = arrowCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, arrowCanvas.getWidth(), arrowCanvas.getHeight());
    }

    public Node highlightSquare(int col, int row, Color color) {
        Rectangle highlight = new Rectangle(TILE_SIZE, TILE_SIZE);
        highlight.setFill(Color.TRANSPARENT); // Transparent fill color
        highlight.setStroke(color); // Border color
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

    public void setPosition(String fen, Move lastMove) {
        chessBoard.loadFromFen(fen);
        updateBoard(chessBoard, lastMove, pieceStyle);
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
            Move move = parseMoveFromString(moveStr, chessBoard.getFen());
            if (move != null) {
                chessBoard.doMove(move);
                updateBoard(chessBoard, move, pieceStyle);
                currentMoveIndex++;
            }
        }
    }

    public void previousMove() {
        if (moveList != null && currentMoveIndex > 0) {
            chessBoard.undoMove();
            currentMoveIndex--;
            updateBoard(chessBoard, null, pieceStyle); // Nessun highlight per la mossa precedente
        }
    }

    private Move parseMoveFromString(String moveStr, String fen) {
        Square from = Square.valueOf(moveStr.substring(0, 2).toUpperCase());
        Square to = Square.valueOf(moveStr.substring(2, 4).toUpperCase());

        // Extract the side to move from the FEN string
        boolean isWhiteToMove = fen.split(" ")[1].equals("w");

        if (moveStr.length() == 5) { // Handle pawn promotion
            char promotionChar = moveStr.charAt(4);
            Piece promotionPiece = getPromotionPiece(promotionChar, isWhiteToMove);
            return new Move(from, to, promotionPiece);
        }

        return new Move(from, to);
    }

    private Piece getPromotionPiece(char promotionChar, boolean isWhite) {
        switch (Character.toLowerCase(promotionChar)) {
            case 'q':
                return isWhite ? Piece.WHITE_QUEEN : Piece.BLACK_QUEEN;
            case 'r':
                return isWhite ? Piece.WHITE_ROOK : Piece.BLACK_ROOK;
            case 'b':
                return isWhite ? Piece.WHITE_BISHOP : Piece.BLACK_BISHOP;
            case 'n':
                return isWhite ? Piece.WHITE_KNIGHT : Piece.BLACK_KNIGHT;
            default:
                throw new IllegalArgumentException("Invalid promotion piece: " + promotionChar);
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

}