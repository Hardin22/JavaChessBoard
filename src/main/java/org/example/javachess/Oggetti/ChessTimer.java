package org.example.javachess.Oggetti;

import javafx.application.Platform;
import javafx.scene.control.Label;
import java.util.Timer;
import java.util.TimerTask;

public class ChessTimer {
    private Timer whiteTimer;
    private Timer blackTimer;
    private Label whiteLabel;
    private Label blackLabel;
    private int whiteTime;
    private int blackTime;
    private int increment;

    public ChessTimer(Label whiteLabel, Label blackLabel, int gameDuration, int increment) {
        this.whiteLabel = whiteLabel;
        this.blackLabel = blackLabel;
        this.whiteTime = gameDuration;
        this.blackTime = gameDuration;
        this.increment = increment;
    }
    public void initializetimer() {
        updateLabel(whiteLabel, whiteTime);
        updateLabel(blackLabel, blackTime);
    }

    public void startWhiteTimer() {
        stopBlackTimer();
        whiteTimer = new Timer();
        whiteTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (whiteTime > 0) {
                        whiteTime--;
                        updateLabel(whiteLabel, whiteTime);
                    }
                });
            }
        }, 0, 1000);
    }

    public void startBlackTimer() {
        stopWhiteTimer();
        blackTimer = new Timer();
        blackTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (blackTime > 0) {
                        blackTime--;
                        updateLabel(blackLabel, blackTime);
                    }
                });
            }
        }, 0, 1000);
    }

    public void addIncrementToWhite() {
        whiteTime += increment;
        updateLabel(whiteLabel, whiteTime); // Aggiorna il timer sulla UI
        System.out.println("whiteTime: " + whiteTime);
    }

    public void addIncrementToBlack() {
        blackTime += increment;
        updateLabel(blackLabel, blackTime); // Aggiorna il timer sulla UI
        System.out.println("blackTime: " + blackTime);
    }

    public void stopWhiteTimer() {
        if (whiteTimer != null) {
            whiteTimer.cancel();
            whiteTimer = null;
        }
    }

    public void stopBlackTimer() {
        if (blackTimer != null) {
            blackTimer.cancel();
            blackTimer = null;
        }
    }

    private void updateLabel(Label label, int time) {
        int minutes = time / 60;
        int seconds = time % 60;
        label.setText(String.format("%02d:%02d", minutes, seconds));
    }

}
