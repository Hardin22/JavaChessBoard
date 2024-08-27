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
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private Task<Void> moveCalculationTask;
    private EvalBar evalBar;
    private StringBuilder pgn; // Variabile per memorizzare il PGN
    private int gameId;
    private Path archivePath;
    private boolean saveGame= true;

    public PvpGame(ChessBoardUI chessBoardUI, Label evaluationLabel, EvalBar evalBar, Label move1Label, Label move2Label, Label move3Label, Label whiteLabel, Label blackLabel, int gameDuration, int increment) {
        this.board = new Board();
        this.chessBoardUI = chessBoardUI;
        this.evaluationLabel = evaluationLabel;
        this.evalBar = evalBar;
        this.move1Label = move1Label;
        this.move2Label = move2Label;
        this.move3Label = move3Label;
        this.increment = increment;
        this.chessTimer = new ChessTimer(whiteLabel, blackLabel, gameDuration, increment, this);
        this.pgn = new StringBuilder(); // Inizializza la stringa PGN
        this.archivePath = copyArchiveJsonToWritableLocation();

        // Imposta il gameId come l'ultimo ID nel file JSON + 1
        this.gameId = getNextGameId();
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
        pgn.setLength(0); // Resetta la stringa PGN per la nuova partita
        evaluatePositionAndMoves();
    }

    public Stockfish getStockfish(){
        return this.stockfish;
    }

    public Board getBoard() {
        return this.board;
    }
    public String getEvaluationLabel(){
        return evaluationLabel.getText();
    }



    private Path copyArchiveJsonToWritableLocation() {
        Path targetPath = Paths.get("archive.json");

        if (!Files.exists(targetPath)) {
            try (InputStream resourceStream = getClass().getResourceAsStream("/archive.json")) {
                if (resourceStream == null) {
                    throw new IllegalArgumentException("archive.json not found in resources");
                }
                Files.copy(resourceStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return targetPath;
    }

    private int getNextGameId() {
        int nextId = 1; // Valore di default se non ci sono partite salvate

        try {
            if (Files.exists(archivePath)) {
                String content = new String(Files.readAllBytes(archivePath));
                JSONArray gamesArray = new JSONArray(content);

                for (int i = 0; i < gamesArray.length(); i++) {
                    JSONObject game = gamesArray.getJSONObject(i);
                    int id = game.getInt("id");
                    if (id >= nextId) {
                        nextId = id + 1;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return nextId;
    }

    private void evaluatePositionAndMoves() {
        if (!gameRunning) return;

        if (moveCalculationTask != null && moveCalculationTask.isRunning()) {
            moveCalculationTask.cancel();  // Annulla il task precedente se è ancora in esecuzione
        }

        moveCalculationTask = new Task<Void>() {
            @Override
            protected Void call() {
                stockfish.getTopThreeMoves(board.getFen(),move1Label, move2Label, move3Label, chessBoardUI, evaluationLabel, evalBar);
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
                updatePgn(move); // Aggiorna la stringa PGN con la nuova mossa

                Platform.runLater(() -> {
                    chessBoardUI.setPosition(board.getFen(), move);
                    System.out.println(board.getHalfMoveCounter());

                    if (board.isMated()) {
                        saveGame=false;
                        String winner = board.getSideToMove().flip() == Side.WHITE ? "Bianco" : "Nero";
                        endGame("Scaccomatto! Vince il " + winner + ".", true);
                    } else if (board.isDraw() || board.getHalfMoveCounter() >= 100) {
                        saveGame=false;
                        String drawReason = getDrawReason();
                        evaluationLabel.setText("0.00");
                        evalBar.updateEvaluation(0.00);
                        endGame(drawReason, true);
                    } else {
                        // Se il gioco non è finito, cambia il turno e aggiorna il timer
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
                        evaluatePositionAndMoves();
                    }
                });
            } else {
                System.out.println("Mossa illegale o non valida, riprova.");
            }
        } catch (Exception e) {
            System.out.println("Errore: Mossa non valida. Riprova.");
        }
    }

    private void updatePgn(Move move) {
        // Aggiorna la stringa PGN con la nuova mossa
        if (isWhiteTurn) {
            pgn.append(board.getMoveCounter()).append(". ").append(move.toString()).append(" ");
        } else {
            pgn.append(move.toString()).append(" ");
        }
    }

    private String getDrawReason() {
        if (board.isStaleMate()) {
            return "Patta per Stallo.";
        } else if (board.isRepetition()) {
            return "Patta per Triplice Ripetizione.";
        } else if (board.isInsufficientMaterial()) {
            return "Patta per Materiale Insufficiente.";
        } else if (board.getHalfMoveCounter() >= 100) {
            return "Patta per la Regola delle 50 Mosse.";
        } else {
            return "Patta.";
        }
    }
    public boolean isSaveGame() {
        return saveGame;
    }

    public void endGame(String endMessage, boolean saveGame) {
        gameRunning = false;
        // Ferma i timer
        chessTimer.stopWhiteTimer();
        chessTimer.stopBlackTimer();
        if (pgn.length() < 20){
            System.out.println("Partita non salvata, mossa troppo breve");
            saveGame=false;
        }

        // Salva la partita in formato PGN
        if (saveGame) {
            saveGameToJson(endMessage);
        }

        // Chiudi Stockfish se è attivo
        if (stockfish != null) {
            stockfish.close();
            stockfish = null;
            System.out.println("Stockfish chiuso.");
        }

        // Cancella eventuali calcoli di mosse in corso
        if (moveCalculationTask != null && moveCalculationTask.isRunning()) {
            moveCalculationTask.cancel();  // Annulla qualsiasi calcolo in corso
        }

        // Aggiorna le etichette con il messaggio di fine partita
        move1Label.setText(endMessage);
        move2Label.setText("");
        move3Label.setText("");
    }

    private void saveGameToJson(String result) {
        pgn.append(" ").append(result);
        System.out.println("Partita salvata in formato PGN: " + pgn.toString());

            try {
                JSONObject gameJson = new JSONObject();
                gameJson.put("id", gameId);
                gameJson.put("pgn", pgn.toString());
                gameJson.put("result", result);

                // Get current date and time
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
                String formattedDateTime = now.format(formatter);

                // Add formatted date and time to JSON
                gameJson.put("datetime", formattedDateTime);

                // Leggi il file JSON esistente o creane uno nuovo
                JSONArray gamesArray;
                if (Files.exists(archivePath)) {
                    String content = new String(Files.readAllBytes(archivePath));
                    gamesArray = new JSONArray(content);
                } else {
                    gamesArray = new JSONArray();
                }

                gamesArray.put(gameJson);

                // Scrivi l'array aggiornato nel file JSON
                Files.write(archivePath, gamesArray.toString(4).getBytes());

            } catch (IOException e) {
                e.printStackTrace();
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
