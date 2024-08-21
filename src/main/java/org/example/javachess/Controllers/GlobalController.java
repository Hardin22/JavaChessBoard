package org.example.javachess.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.example.javachess.Oggetti.ChessBoardUI;
import org.example.javachess.Oggetti.PvpGame;

public class GlobalController {

    private PvpGame pvpGame;
    private ChessBoardUI chessBoard;

    @FXML
    private VBox home;
    @FXML
    private VBox pvp;
    @FXML
    private VBox pvpgame;
    @FXML
    private Label evaluationLabel;

    @FXML
    void initialize() {
        System.out.println("Global Controller initialized");
        chessBoard = new ChessBoardUI();
        pvpgame.getChildren().add(2, chessBoard);
    }

    @FXML
    private void showPvPpage() {
        System.out.println("Show Player vs Player page");
        home.setVisible(false);
        pvp.setVisible(true);
    }

    @FXML
    private void backtopvp() {
        System.out.println("Back to Player vs Player page");
        if (pvpGame != null) {
            pvpGame.endGame();
            pvpGame = null; // Assicurati di resettare la variabile
        }
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

        pvpGame = new PvpGame(chessBoard, evaluationLabel , move1Label, move2Label, move3Label, whiteLabel, blackLabel, 600, 20);
        pvpGame.startGame();
    }
    @FXML
    private TextField moveInputField;

    @FXML
    private void handleMoveInput() {
        String moveInput = moveInputField.getText().trim();
        if (!moveInput.isEmpty() && pvpGame != null) {
            pvpGame.handleMoveInput(moveInput);
            moveInputField.clear(); // Pulisce il campo di input dopo aver inviato la mossa
        }
    }

}
