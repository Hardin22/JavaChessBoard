package org.example.javachess.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.example.javachess.Oggetti.ChessBoardUI;
import org.example.javachess.Oggetti.PvpGame;
import org.example.javachess.Oggetti.PvcGame;
import org.example.javachess.Oggetti.WebViewExample;
import java.io.IOException;

public class GlobalController {

    private PvpGame pvpGame;
    private PvcGame pvcGame; // Aggiunta della variabile pvcGame
    private ChessBoardUI chessBoard;
    private ChessBoardUI chessBoardpvc;

    @FXML
    private VBox home;
    @FXML
    private VBox pvp;
    @FXML
    private VBox pvpgame;
    @FXML
    private Label evaluationLabel;
    @FXML
    private VBox pvcgame;
    @FXML
    private VBox pvcsetup;
    @FXML
    private VBox scacchiera;
    @FXML
    private VBox StatsComponent;
    @FXML
    void initialize() {
        System.out.println("Global Controller initialized");
        chessBoard = new ChessBoardUI();
        pvpgame.getChildren().add(2, chessBoard);
        chessBoardpvc = new ChessBoardUI();
        scacchiera.getChildren().add(chessBoardpvc);
    }

    @FXML
    private void showPvPpage() {
        System.out.println("Show Player vs Player page");
        home.setVisible(false);
        pvp.setVisible(true);
    }
    @FXML
    private StackPane main; // Assicurati di avere un riferimento al tuo StackPane principale
    @FXML

    private void startLichess() {
        WebViewExample.openLichessInMain(main); // Dove stage è lo Stage attuale della tua applicazione
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
            pvpGame = null; // Assicurati di resettare la variabile
        }
        pvpgame.getChildren().remove(StatsComponent); // Rimuovi il campo di input
        chessBoard.resetBoard(); // Resetta la scacchiera
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

        pvpGame = new PvpGame(chessBoard, evaluationLabel, move1Label, move2Label, move3Label, whiteLabel, blackLabel, 600, 20);
        pvpGame.startGame();
    }

    @FXML
    private TextField moveInputField;

    @FXML

    //ATTENZIONE QUI QUI QUI QUI QUIQ UI
    private void handleMoveInput() {
        String moveInput = moveInputField.getText().trim();
        if (!moveInput.isEmpty()) {
            if (pvpGame != null) {
                pvpGame.handleMoveInput(moveInput);
            } else if (pvcGame != null) {
                pvcGame.handleMoveInput(moveInput); // Aggiunta della gestione per PvC
            }
            moveInputField.clear(); // Pulisce il campo di input dopo aver inviato la mossa
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
            pvcGame = null; // Assicurati di resettare la variabile
        }
        pvcgame.setVisible(false);
        pvcsetup.setVisible(true);
        pvcgame.getChildren().remove(StatsComponent);
        chessBoardpvc.resetBoard();

    }

    @FXML
    private void iniziaPvC() {
        System.out.println("Start Player vs Computer game");
        if (pvcGame != null) {
            pvcGame.endGame();
            pvcGame = null; // Assicurati di resettare la variabile
        }

        pvcsetup.setVisible(false);
        pvcgame.setVisible(true);

        boolean isPlayerWhite = true; // o false se il giocatore vuole essere nero
        int skillLevel = 1; // Imposta il livello di difficoltà da 0 a 20

        pvcGame = new PvcGame(chessBoardpvc, evaluationLabelpvc, move1Labelpvc, move2Labelpvc, move3Labelpvc, isPlayerWhite, skillLevel);
        pvcGame.startGame();
    }
    @FXML
    private void handleMoveInputpvc() {
        String moveInput = moveInputFieldpvc.getText().trim();
        if (!moveInput.isEmpty()) {
            if (pvcGame != null) {
                pvcGame.handleMoveInput(moveInput);
            }
            moveInputFieldpvc.clear(); // Pulisce il campo di input dopo aver inviato la mossa
        }
    }


}
