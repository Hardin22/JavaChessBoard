package org.example.javachess.Application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Carica il file FXML
            Parent root = FXMLLoader.load(App.class.getResource("/UI/UI.fxml"));
            Font.loadFont(App.class.getResource("/Font/Poppins/Poppins-Regular.ttf").toExternalForm(), 10);
            Font.loadFont(App.class.getResource("/Font/Poppins/Poppins-Medium.ttf").toExternalForm(), 10);
            Font.loadFont(App.class.getResource("/Font/Poppins/Poppins-Bold.ttf").toExternalForm(), 10);


            // Configura la scena
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            scene.getStylesheets().add(App.class.getResource("/Styles/Style.css").toExternalForm());
            primaryStage.setTitle("Chess Application");
            primaryStage.setFullScreen(true);

            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}