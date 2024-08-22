package org.example.javachess.Oggetti;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.javachess.Utils.LichessAPIHelper;

public class LichessMoveExecutor extends Application {

    private static final String TOKEN = "stop-it-get-some-help";
    private static String gameId;

    public static void setGameId(String id) {
        gameId = id; // Imposta l'ID della partita
        Platform.runLater(() -> {
            try {
                new LichessMoveExecutor().start(new Stage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void start(Stage primaryStage) {
        Label label = new Label("Inserisci la tua mossa:");
        TextField moveField = new TextField();
        Button submitButton = new Button("Esegui Mossa");

        submitButton.setOnAction(e -> {
            String move = moveField.getText();
            if (!move.isEmpty() && gameId != null) {
                try {
                    sendMoveToLichess(move);
                    label.setText("Mossa eseguita: " + move);
                } catch (Exception ex) {
                    label.setText("Errore durante l'esecuzione della mossa.");
                    ex.printStackTrace();
                }
            } else if (gameId == null) {
                label.setText("Errore: ID della partita non trovato.");
            }
        });

        VBox vbox = new VBox(label, moveField, submitButton);
        Scene scene = new Scene(vbox, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void sendMoveToLichess(String move) throws Exception {
        String urlString = "https://lichess.org/api/board/game/" + gameId + "/move/" + move;
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + TOKEN);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(0);  // Il contenuto del POST è vuoto
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("Errore: " + responseCode);
        }
    }
}
