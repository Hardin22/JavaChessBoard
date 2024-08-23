package org.example.javachess.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.web.WebView;
import org.example.javachess.Oggetti.ChessBoardUI;
import org.example.javachess.Oggetti.PvpGame;
import org.example.javachess.Oggetti.PvcGame;
import org.example.javachess.Oggetti.WebViewExample;
import org.example.javachess.Oggetti.EvalBar;

import java.io.IOException;

public class GlobalController {

    private PvpGame pvpGame;
    private PvcGame pvcGame;
    private ChessBoardUI chessBoard;
    private ChessBoardUI chessBoardpvc;

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

    private String selectedColor = "random";

    private EvalBar evalBarPvP;
    private EvalBar evalBarPvC;

    private int durata = 10;
    private int incremento = 0;

    @FXML
    void initialize() {
        System.out.println("Global Controller initialized");

        // Caricamento delle immagini
        Image boardicon = new Image(getClass().getResource("/images/boardicon.PNG").toExternalForm());
        boardlogo.setImage(boardicon);

        Image robotImage = new Image(getClass().getResource("/images/Robot.png").toExternalForm());
        robotButtonImageView.setImage(robotImage);

        Image onlineImage = new Image(getClass().getResource("/images/Online.png").toExternalForm());
        onlineButtonImageView.setImage(onlineImage);

        Image friendImage = new Image(getClass().getResource("/images/image.png").toExternalForm());
        friendButtonImageView.setImage(friendImage);

        // Ascoltatori per slider
        durataSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            durata = newValue.intValue();
            durataLabel.setText("durata: " + durata + "min");
        });
        incrementoSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            incremento = newValue.intValue();
            incrementoLabel.setText("incremento: " + incremento + "s");
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
    }

    @FXML
    private void backtopvp() {
        System.out.println("Back to Player vs Player page");
        if (pvpGame != null) {
            pvpGame.endGame();
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
        if (pvpGame != null) {
            pvpGame.endGame();
        }
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
            pvcGame.endGame();
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
        if (pvcGame != null) {
            pvcGame.endGame();
            pvcGame = null;
        }

        pvcsetup.setVisible(false);
        pvcgame.setVisible(true);
        if (selectedColor.equals("white")) {
            isPlayerWhite = true;
        } else if (selectedColor.equals("black")) {
            isPlayerWhite = false;
        } else {
            isPlayerWhite = Math.random() < 0.5;
        }

        int skillLevel = 1;

        pvcGame = new PvcGame(chessBoardpvc, evaluationLabelpvc, evalBarPvC, move1Labelpvc, move2Labelpvc, move3Labelpvc, isPlayerWhite, skillLevel);
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
}
