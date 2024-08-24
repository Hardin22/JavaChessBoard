package org.example.javachess.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Arc;
import javafx.stage.Stage;
import javafx.scene.web.WebView;
import org.example.javachess.Oggetti.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.example.javachess.Oggetti.Stockfish;
import java.nio.file.StandardCopyOption;



import java.io.IOException;

public class GlobalController {

    private PvpGame pvpGame;
    private PvcGame pvcGame;
    private ChessBoardUI chessBoard;
    private ChessBoardUI chessBoardpvc;
    private ChessBoardUI reviewChessBoard;
    private EvalBar reviewEvalBar;
    private Stockfish stockfish;

    @FXML
    private VBox home;
    @FXML
    private BorderPane pvp;
    @FXML
    private BorderPane pvpgame;
    @FXML
    private Label evaluationLabel;
    @FXML
    private VBox pvcgame;
    @FXML
    private BorderPane pvcsetup;
    @FXML
    private HBox scacchiera;
    @FXML
    private ImageView boardlogo;
    @FXML
    private ImageView robotButtonImageView;
    @FXML
    private ImageView onlineButtonImageView;
    @FXML
    private ImageView friendButtonImageView;
    @FXML
    private HBox scacchierapvp;
    @FXML
    private Label durataLabel;
    @FXML
    private Slider durataSlider;
    @FXML
    private Label incrementoLabel;
    @FXML
    private Slider incrementoSlider;
    @FXML
    private Slider difficoltàslider;
    @FXML
    private ToggleButton whiteButton;
    @FXML
    private ToggleButton blackButton;
    @FXML
    private ToggleButton randomButton;
    @FXML
    private VBox randomVbox;
    @FXML
    private VBox whiteVbox;
    @FXML
    private VBox blackVbox;
    @FXML
    private Label difficoltàLabel;
    @FXML
    private BorderPane Archive;
    @FXML
    private VBox archivevbox;
    @FXML
    private ScrollPane archiveScroll;
    @FXML
    private BorderPane GameReviewpage;
    @FXML
    private VBox reviewVbox;
    @FXML
    private HBox reviewHbox;

    private String selectedColor = "random";

    private EvalBar evalBarPvP;
    private EvalBar evalBarPvC;

    private int durata = 10;
    private int incremento = 0;
    private int skillLevel = 10;


    @FXML
    void initialize() {
        System.out.println("Global Controller initialized");

        try (InputStream boardiconStream = GlobalController.class.getResourceAsStream("/images/boardicon.png")) {
            if (boardiconStream == null) {
                throw new IllegalArgumentException("Immagine non trovata: /images/boardicon.PNG");
            }
            Image boardicon = new Image(boardiconStream);
            boardlogo.setImage(boardicon);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (InputStream robotImageStream = GlobalController.class.getResourceAsStream("/images/Robot.PNG")) {
            if (robotImageStream == null) {
                throw new IllegalArgumentException("Immagine non trovata: /images/Robot.PNG");
            }
            Image robotImage = new Image(robotImageStream);
            robotButtonImageView.setImage(robotImage);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (InputStream onlineImageStream = GlobalController.class.getResourceAsStream("/images/Online.png")) {
            if (onlineImageStream == null) {
                throw new IllegalArgumentException("Immagine non trovata: /images/Online.png");
            }
            Image onlineImage = new Image(onlineImageStream);
            onlineButtonImageView.setImage(onlineImage);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (InputStream friendImageStream = GlobalController.class.getResourceAsStream("/images/image.png")) {
            if (friendImageStream == null) {
                throw new IllegalArgumentException("Immagine non trovata: /images/image.png");
            }
            Image friendImage = new Image(friendImageStream);
            friendButtonImageView.setImage(friendImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Ascoltatori per slider
        durataSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            durata = newValue.intValue();
            durataLabel.setText("durata: " + durata + "min");
        });
        incrementoSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            incremento = newValue.intValue();
            incrementoLabel.setText("incremento: " + incremento + "s");
        });
        difficoltàslider.valueProperty().addListener((observable, oldValue, newValue) -> {
            skillLevel = newValue.intValue();
            difficoltàLabel.setText("Difficoltà: " + skillLevel);
        });

        ToggleGroup colorGroup = new ToggleGroup();
        whiteButton.setToggleGroup(colorGroup);
        blackButton.setToggleGroup(colorGroup);
        randomButton.setToggleGroup(colorGroup);

        // Listener for updating the selected color and adding/removing the border
        colorGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            // Remove purple border from all VBoxes
            whiteVbox.getStyleClass().remove("vbox-border-purple");
            blackVbox.getStyleClass().remove("vbox-border-purple");
            randomVbox.getStyleClass().remove("vbox-border-purple");

            if (newToggle == whiteButton) {
                selectedColor = "white";
                whiteVbox.getStyleClass().add("vbox-border-purple");
            } else if (newToggle == blackButton) {
                selectedColor = "black";
                blackVbox.getStyleClass().add("vbox-border-purple");
            } else if (newToggle == randomButton) {
                selectedColor = "random";
                randomVbox.getStyleClass().add("vbox-border-purple");
            }
        });

        // Inizializzazione della scacchiera PvP e EvalBar
        chessBoard = new ChessBoardUI();
        evalBarPvP = new EvalBar(20, 400);
        scacchierapvp.getChildren().addAll(chessBoard, evalBarPvP);

        // Inizializzazione della scacchiera PvC e EvalBar
        chessBoardpvc = new ChessBoardUI();
        evalBarPvC = new EvalBar(20, 400);
        scacchiera.getChildren().addAll(chessBoardpvc, evalBarPvC);

    }


    @FXML
    private void showPvPpage() {
        System.out.println("Show Player vs Player page");
        home.setVisible(false);
        pvp.setVisible(true);
    }

    @FXML
    private StackPane main;

    @FXML
    private void startLichess() {
        WebViewExample.openLichessInMain(main);
    }

    @FXML
    private void backtohome() {
        pvp.setVisible(false);
        pvcsetup.setVisible(false);
        home.setVisible(true);
        Archive.setVisible(false);
        archivevbox.getChildren().clear();
    }

    @FXML
    private void backtopvp() {
        System.out.println("Back to Player vs Player page");
        if (pvpGame != null) {
            boolean saveGame = pvpGame.isSaveGame();
            pvpGame.endGame("Partita interrotta", saveGame);
            pvpGame = null;
        }
        chessBoard.resetBoard();
        pvpgame.setVisible(false);
        pvp.setVisible(true);
    }

    @FXML
    private Label move1Label;
    @FXML
    private Label move2Label;
    @FXML
    private Label move3Label;
    @FXML
    private Label whiteLabel;
    @FXML
    private Label blackLabel;

    @FXML
    private void iniziaPvP() {
        System.out.println("Start Player vs Player game");
        pvp.setVisible(false);
        pvpgame.setVisible(true);
        chessBoard.resetBoard();

        pvpGame = new PvpGame(chessBoard, evaluationLabel, evalBarPvP, move1Label, move2Label, move3Label, whiteLabel, blackLabel, durata*60, incremento);
        pvpGame.startGame();
    }

    @FXML
    private TextField moveInputField;

    @FXML
    private void handleMoveInput() {
        String moveInput = moveInputField.getText().trim();
        if (!moveInput.isEmpty()) {
            if (pvpGame != null) {
                pvpGame.handleMoveInput(moveInput);
            } else if (pvcGame != null) {
                pvcGame.handleMoveInput(moveInput);
            }
            moveInputField.clear();
        }
    }

    @FXML
    private Label move1Labelpvc;
    @FXML
    private Label move2Labelpvc;
    @FXML
    private Label move3Labelpvc;
    @FXML
    private Label evaluationLabelpvc;
    @FXML
    private TextField moveInputFieldpvc;

    @FXML
    private void showpvc() {
        System.out.println("Show Player vs Computer page");
        home.setVisible(false);
        pvcsetup.setVisible(true);
    }

    @FXML
    private void backtopvc() {
        System.out.println("Back to Player vs Computer page");
        if (pvcGame != null) {
            boolean saveGame = pvcGame.isSaveGame();
            pvcGame.endGame(saveGame);
            pvcGame = null;
        }
        pvcgame.setVisible(false);
        pvcsetup.setVisible(true);
        chessBoardpvc.resetBoard();
    }

    @FXML
    private void iniziaPvC() {
        System.out.println("Start Player vs Computer game");
        boolean isPlayerWhite;

        pvcsetup.setVisible(false);
        pvcgame.setVisible(true);
        if (selectedColor.equals("white")) {
            isPlayerWhite = true;
        } else if (selectedColor.equals("black")) {
            isPlayerWhite = false;
        } else {
            isPlayerWhite = Math.random() < 0.5;
        }


        pvcGame = new PvcGame(chessBoardpvc, evaluationLabelpvc, evalBarPvC, move1Labelpvc, move2Labelpvc, move3Labelpvc, isPlayerWhite, skillLevel);
        System.out.println("skillLevel: " + skillLevel);
        pvcGame.startGame();
    }

    @FXML
    private void handleMoveInputpvc() {
        String moveInput = moveInputFieldpvc.getText().trim();
        if (!moveInput.isEmpty()) {
            if (pvcGame != null) {
                pvcGame.handleMoveInput(moveInput);
            }
            moveInputFieldpvc.clear();
        }
    }

    @FXML
    private void toggleevaluation() {
        if (evalBarPvP.isVisible()) {
            evalBarPvP.setVisible(false);
            evaluationLabel.setVisible(false);
        } else {
            evalBarPvP.setVisible(true);
            evaluationLabel.setVisible(true);

        }
    }
    @FXML
    private void togglebestmoves() {
        if (move1Label.isVisible()) {
            move1Label.setVisible(false);
            move2Label.setVisible(false);
            move3Label.setVisible(false);
        } else {
            move1Label.setVisible(true);
            move2Label.setVisible(true);
            move3Label.setVisible(true);
        }
    }
    @FXML
    private void showArchivePage() {
        home.setVisible(false);
        Archive.setVisible(true);

        loadArchive();
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
    private void loadArchive() {
        Path archivePath = copyArchiveJsonToWritableLocation();

        try {
            if (Files.exists(archivePath)) {
                String content = new String(Files.readAllBytes(archivePath));
                JSONArray gamesArray = new JSONArray(content);

                for (int i = 0; i < gamesArray.length(); i++) {
                    JSONObject game = gamesArray.getJSONObject(i);
                    int gameId = game.getInt("id");
                    String datetime = game.getString("datetime");
                    String pgn = game.getString("pgn");
                    String result = game.getString("result");

                    // Creazione del Button per ciascuna partita
                    Button gameButton = new Button(datetime + " - " + result);
                    gameButton.setId("gameButton" + gameId);
                    gameButton.setOnAction(event -> showArchiveGame(gameId, pgn));

                    // Aggiunta del Button al VBox
                    archivevbox.getChildren().add(gameButton);
                }
            } else {
                System.out.println("Il file archive.json non esiste.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void showArchiveGame(int gameId, String pgn) {
        stockfish = new Stockfish("/opt/homebrew/bin/stockfish");
        // Show the Game Review page
        Archive.setVisible(false);
        GameReviewpage.setVisible(true);

        // Initialize the review chessboard and add it to the VBox
        reviewChessBoard = new ChessBoardUI();
        reviewEvalBar = new EvalBar(20, 400); // Initialize the EvalBar

        reviewHbox.getChildren().add(reviewChessBoard);
        reviewHbox.getChildren().add(reviewEvalBar); // Add the EvalBar to the VBox
        reviewChessBoard.loadPgn(pgn);

        // Evaluate the current position and update the EvalBar
        updateReviewEvalBar();
    }

    @FXML
    private void backtoarchive() {
        Archive.setVisible(true);
        GameReviewpage.setVisible(false);

        // Clean up the review chessboard and EvalBar
        if (reviewChessBoard != null) {
            reviewHbox.getChildren().remove(reviewChessBoard);
            reviewChessBoard = null; // Helps with garbage collection
        }

        if (reviewEvalBar != null) {
            reviewHbox.getChildren().remove(reviewEvalBar);
            reviewEvalBar = null; // Helps with garbage collection
        }

        // Close Stockfish when exiting the review page
        if (stockfish != null) {
            stockfish.close();
        }
    }

    @FXML
    private void previousMove() {
        if (reviewChessBoard != null) {
            reviewChessBoard.previousMove();
            updateReviewEvalBar(); // Update the EvalBar after moving
        }
    }

    @FXML
    private void nextMove() {
        if (reviewChessBoard != null) {
            reviewChessBoard.nextMove();
            updateReviewEvalBar(); // Update the EvalBar after moving
        }
    }

    private void updateReviewEvalBar() {
        if (reviewChessBoard != null && reviewEvalBar != null) {
            String currentFen = reviewChessBoard.getFen();
            stockfish.evaluatePosition(currentFen, evaluationLabel, reviewEvalBar);
        }
    }
}
