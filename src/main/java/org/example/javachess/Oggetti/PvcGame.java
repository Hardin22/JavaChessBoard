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
    private EvalBar evalBar;
    private StringBuilder pgn; // Variabile per memorizzare il PGN
    private int gameId;
    private Path archivePath;
    public boolean saveGame = true;

    public PvcGame(ChessBoardUI chessBoardUI, Label evaluationLabel, EvalBar evalBar, Label move1Label, Label move2Label, Label move3Label, boolean isPlayerWhite, int skillLevel) {
        this.board = new Board();
        this.chessBoardUI = chessBoardUI;
        this.evaluationLabel = evaluationLabel;
        this.evalBar = evalBar;
        this.move1Label = move1Label;
        this.move2Label = move2Label;
        this.move3Label = move3Label;
        this.isPlayerWhite = isPlayerWhite;
        this.stockfish = new Stockfish("/opt/homebrew/bin/stockfish");
        this.playerStockfish = new Stockfish("/opt/homebrew/bin/stockfish");
        this.playerStockfish.setSkillLevel(skillLevel);
        this.pgn = new StringBuilder(); // Inizializza la stringa PGN
        this.archivePath = copyArchiveJsonToWritableLocation();

        // Imposta il gameId come l'ultimo ID nel file JSON + 1
        this.gameId = getNextGameId();

    }
    public boolean isSaveGame() {
        return saveGame;
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

    public void startGame() {
        gameRunning = true;
        move1Label.setText("");
        move2Label.setText("");
        move3Label.setText("");

        Platform.runLater(() -> chessBoardUI.setPosition(board.getFen(), null));
        evaluatePositionAndMoves();

        if (!isPlayerWhite) {
            handleComputerMove();
        }
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

    private void evaluatePositionAndMoves() {
        if (!gameRunning) return;

        if (moveCalculationTask != null && moveCalculationTask.isRunning()) {
            moveCalculationTask.cancel();  // Annulla il task precedente se è ancora in esecuzione
        }

        moveCalculationTask = new Task<Void>() {
            @Override
            protected Void call() {
                stockfish.getTopThreeMoves(board.getFen(), move1Label, move2Label, move3Label, chessBoardUI, evaluationLabel, evalBar);
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
                Platform.runLater(() -> chessBoardUI.setPosition(board.getFen(), move));

                if (board.isMated()) {
                    String winner = board.getSideToMove().flip() == Side.WHITE ? "Bianco" : "Nero";
                    endGameWithMessage("Scaccomatto! Vince il " + winner + ".");
                } else if (board.isDraw()) {
                    String drawReason = getDrawReason();
                    endGameWithMessage("Partita patta per " + drawReason + ".");
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

    private void updatePgn(Move move) {
        // Aggiorna la stringa PGN con la nuova mossa
        if (isPlayerWhite) {
            pgn.append(board.getMoveCounter()).append(". ").append(move.toString()).append(" ");
        } else {
            pgn.append(move.toString()).append(" ");
        }
    }

    private String getDrawReason() {
        if (board.isStaleMate()) {
            return "Stallo";
        } else if (board.isRepetition()) {
            return "Triplice ripetizione";
        } else if (board.isInsufficientMaterial()) {
            return "Materiale insufficiente";
        } else {
            return "Altro";
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
                    updatePgn(bestMove); // Aggiorna la stringa PGN con la nuova mossa

                    // Aggiorna la scacchiera e calcola le nuove mosse
                    Platform.runLater(() -> {
                        chessBoardUI.setPosition(board.getFen(), bestMove);

                        if (board.isMated()) {
                            String winner = board.getSideToMove().flip() == Side.WHITE ? "Bianco" : "Nero";
                            endGameWithMessage("Scaccomatto! Vince il " + winner + ".");
                        } else if (board.isDraw()) {
                            String drawReason = getDrawReason();
                            endGameWithMessage("Partita patta per " + drawReason + ".");
                        } else {
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
        evaluationLabel.setText(message);
        move1Label.setText("");
        move2Label.setText("");
        move3Label.setText("");
        saveGameToJson(message);
        saveGame = false;
        endGame(false);
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

    public void endGame(boolean saveGame) {
        gameRunning = false;
        if (pgn.length() < 10){
            saveGame = false;
            System.out.println("Partita non salvata, mossa minima non raggiunta.");
        }
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
        if (saveGame) {
            saveGameToJson("Partita interrotta.");
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
