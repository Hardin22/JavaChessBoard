package org.example.javachess.Controllers;

import com.github.bhlangonijr.chesslib.Square;
import javafx.event.ActionEvent;
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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private ImageView archiveButtonImageView;
    @FXML
    private ImageView themeButtonImageView;
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
    @FXML
    private BorderPane theme;
    @FXML
    private BorderPane themeSelection;
    @FXML
    private ImageView boardpreview;
    @FXML
    private ImageView piecepreview;
    @FXML
    private BorderPane pieceSelection;
    @FXML
    private BorderPane boardSelection;
    @FXML
    private Button Marrone;
    @FXML
    private Button Legno;
    @FXML
    private Button Marghiacciato;
    @FXML
    private Button Bubblegum;
    @FXML
    private Button Checkers;
    @FXML
    private Button Neon;
    @FXML
    private ImageView marroneimage;
    @FXML
    private ImageView legnoimage;
    @FXML
    private ImageView marghiacciatoimage;
    @FXML
    private ImageView bubblegumimage;
    @FXML
    private ImageView checkersimage;
    @FXML
    private ImageView neonimage;
    @FXML
    private Button Legnopieces;
    @FXML
    private Button Vetropieces;
    @FXML
    private Button Classicopieces;
    @FXML
    private Button Neonpieces;
    @FXML
    private Button Bubblegumpieces;
    @FXML
    private Button Spraypieces;
    @FXML
    private ImageView Legnoking;
    @FXML
    private ImageView Vetroking;
    @FXML
    private ImageView Classicoking;
    @FXML
    private ImageView Neonking;
    @FXML
    private ImageView Bubblegumking;
    @FXML
    private ImageView Sprayking;
    @FXML
    private Label move1labelreview;
    @FXML
    private Label move2labelreview;
    @FXML
    private Label move3labelreview;
    @FXML
    private Label evalscorereview;



    private String selectedColor = "random";

    private EvalBar evalBarPvP;
    private EvalBar evalBarPvC;

    private int durata = 10;
    private int incremento = 0;
    private int skillLevel = 10;

    private String pieceStyle = "Legno";
    private String boardStyle = "Legno.png";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Future<?> currentTask = null;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> debounceHandle = null;
    private final int DEBOUNCE_DELAY = 200; // in milliseconds
    private AtomicBoolean isCalculating = new AtomicBoolean(false);



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
        try (InputStream archiveImageStream = GlobalController.class.getResourceAsStream("/images/archive.png")) {
            if (archiveImageStream == null) {
                throw new IllegalArgumentException("Immagine non trovata: /images/archive.png");
            }
            Image archiveimage = new Image(archiveImageStream);
            archiveButtonImageView.setImage(archiveimage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try (InputStream themeImageStream = GlobalController.class.getResourceAsStream("/images/theme.png")) {
            if (themeImageStream == null) {
                throw new IllegalArgumentException("Immagine non trovata: /images/theme.png");
            }
            Image themeImage = new Image(themeImageStream);
            themeButtonImageView.setImage(themeImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try (InputStream marroneImageStream = GlobalController.class.getResourceAsStream("/images/Scacchiere/Marrone.png")) {
            if (marroneImageStream == null) {
                throw new IllegalArgumentException("Immagine non trovata: /images/Scacchiere/Marrone.png");
            }
            Image marroneImage = new Image(marroneImageStream);
            marroneimage.setImage(marroneImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try (InputStream legnoImageStream = GlobalController.class.getResourceAsStream("/images/Scacchiere/Legno.png")) {
            if (legnoImageStream == null) {
                throw new IllegalArgumentException("Immagine non trovata: /images/Scacchiere/Legno.png");
            }
            Image legnoImage = new Image(legnoImageStream);
            legnoimage.setImage(legnoImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try (InputStream marghiacciatoImageStream = GlobalController.class.getResourceAsStream("/images/Scacchiere/Marghiacciato.png")) {
            if (marghiacciatoImageStream == null) {
                throw new IllegalArgumentException("Immagine non trovata: /images/Scacchiere/Marghiacciato.png");
            }
            Image marghiacciatoImage = new Image(marghiacciatoImageStream);
            marghiacciatoimage.setImage(marghiacciatoImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try (InputStream bubblegumImageStream = GlobalController.class.getResourceAsStream("/images/Scacchiere/Bubblegum.png")) {
            if (bubblegumImageStream == null) {
                throw new IllegalArgumentException("Immagine non trovata: /images/Scacchiere/Bubblegum.png");
            }
            Image bubblegumImage = new Image(bubblegumImageStream);
            bubblegumimage.setImage(bubblegumImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try (InputStream checkersImageStream = GlobalController.class.getResourceAsStream("/images/Scacchiere/Checkers.png")) {
            if (checkersImageStream == null) {
                throw new IllegalArgumentException("Immagine non trovata: /images/Scacchiere/Checkers.png");
            }
            Image checkersImage = new Image(checkersImageStream);
            checkersimage.setImage(checkersImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try (InputStream neonImageStream = GlobalController.class.getResourceAsStream("/images/Scacchiere/Neon.png")) {
            if (neonImageStream == null) {
                throw new IllegalArgumentException("Immagine non trovata: /images/Scacchiere/Neon.png");
            }
            Image neonImage = new Image(neonImageStream);
            neonimage.setImage(neonImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try (InputStream boardpreviewImageStream = GlobalController.class.getResourceAsStream("/images/Scacchiere/"+ boardStyle)) {
            if (boardpreviewImageStream == null) {
                throw new IllegalArgumentException("Immagine non trovata: /images/Scacchiere/"+ boardStyle);
            }
            Image boardpreviewImage = new Image(boardpreviewImageStream);
            boardpreview.setImage(boardpreviewImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try (InputStream piecepreviewImageStream = GlobalController.class.getResourceAsStream("/images/preview.png")) {
            if (piecepreviewImageStream == null) {
                throw new IllegalArgumentException("Immagine non trovata: /images/previews.png");
            }
            Image piecepreviewImage = new Image(piecepreviewImageStream);
            piecepreview.setImage(piecepreviewImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try (InputStream legnokingImageStream = GlobalController.class.getResourceAsStream("/images/Pieces/Legno/wk.png")) {
            if (legnokingImageStream == null) {
                throw new IllegalArgumentException("Immagine non trovata: /images/Pieces/Legno/wk.png");
            }
            Image legnokingImage = new Image(legnokingImageStream);
            Legnoking.setImage(legnokingImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try (InputStream vetrokingImageStream = GlobalController.class.getResourceAsStream("/images/Pieces/Vetro/wk.png")) {
            if (vetrokingImageStream == null) {
                throw new IllegalArgumentException("Immagine non trovata: /images/Pieces/Vetro/wk.png");
            }
            Image vetrokingImage = new Image(vetrokingImageStream);
            Vetroking.setImage(vetrokingImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try (InputStream classicokingImageStream = GlobalController.class.getResourceAsStream("/images/Pieces/Classico/wk.png")) {
            if (classicokingImageStream == null) {
                throw new IllegalArgumentException("Immagine non trovata: /images/Pieces/Classico/wk.png");
            }
            Image classicokingImage = new Image(classicokingImageStream);
            Classicoking.setImage(classicokingImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try (InputStream neonkingImageStream = GlobalController.class.getResourceAsStream("/images/Pieces/Neon/wk.png")) {
            if (neonkingImageStream == null) {
                throw new IllegalArgumentException("Immagine non trovata: /images/Pieces/Neon/wk.png");
            }
            Image neonkingImage = new Image(neonkingImageStream);
            Neonking.setImage(neonkingImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try (InputStream bubblegumkingImageStream = GlobalController.class.getResourceAsStream("/images/Pieces/Bubblegum/wk.png")) {
            if (bubblegumkingImageStream == null) {
                throw new IllegalArgumentException("Immagine non trovata: /images/Pieces/Bubblegum/wk.png");
            }
            Image bubblegumkingImage = new Image(bubblegumkingImageStream);
            Bubblegumking.setImage(bubblegumkingImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try (InputStream spraykingImageStream = GlobalController.class.getResourceAsStream("/images/Pieces/Spray/wk.png")) {
            if (spraykingImageStream == null) {
                throw new IllegalArgumentException("Immagine non trovata: /images/Pieces/Spray/wk.png");
            }
            Image spraykingImage = new Image(spraykingImageStream);
            Sprayking.setImage(spraykingImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        legalmovesinput.setOnAction(event -> handleLegalMoves());



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
        Archive.setVisible(false);
        archivevbox.getChildren().clear();
        themeSelection.setVisible(false);
        home.setVisible(true);
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

        // Crea la scacchiera e l'EvalBar qui
        chessBoard = new ChessBoardUI(boardStyle, pieceStyle);
        evalBarPvP = new EvalBar(20, 400);

        // Aggiungi la scacchiera e l'EvalBar al layout
        scacchierapvp.getChildren().clear(); // Pulisci prima il contenitore
        scacchierapvp.getChildren().addAll(chessBoard, evalBarPvP);

        chessBoard.resetBoard();
        pvpGame = new PvpGame(chessBoard, evaluationLabel, evalBarPvP, move1Label, move2Label, move3Label, whiteLabel, blackLabel, durata*60, incremento);
        pvpGame.startGame();
    }
    @FXML
    private TextField legalmovesinput; // L'InputField nel tuo file FXML

    @FXML
    private void handleLegalMoves() {
        String input = legalmovesinput.getText().trim();
        chessBoard.clearHighlights();
        System.out.println("Input: " + input);
        String currentEval = evaluationLabel.getText();
        System.out.println("Current Evaluation: " + currentEval);

        if (input != null && !input.isEmpty()) {
            try {
                Square selectedSquare = Square.valueOf(input.toUpperCase());
                Stockfish currentstockfish = pvpGame.getStockfish();
                // Chiama la funzione di Stockfish per evidenziare le mosse legali

                currentstockfish.highlightLegalMovesWithEvaluation(pvpGame.getBoard(), selectedSquare, chessBoard, getEvaluationFromLabel(pvpGame.getEvaluationLabel()));
            } catch (IllegalArgumentException e) {
                // Gestisci il caso in cui l'input non sia valido
                System.out.println("Input non valido. Inserisci un quadrato valido, ad esempio 'e4'.");
            }
        }
    }
    public double getEvaluationFromLabel(String evalText) {
        try {
            // Controlla se la valutazione indica un matto
            if (evalText.contains("#")) {
                String[] parts = evalText.split(" ");
                for (String part : parts) {
                    if (part.startsWith("#")) {
                        System.out.println("Mate string: " + part);
                        int mateIn = Integer.parseInt(part.substring(1));
                        System.out.println("Mate in: " + mateIn);
                        return mateIn > 0 ? 100.00 : -100.00;
                    }
                }
            } else {
                // Valutazione numerica standard in centesimi di pedone
                String[] parts = evalText.split(" ");
                for (String part : parts) {
                    // Sostituisci la virgola con un punto per gestire i decimali
                    part = part.replace(",", ".");
                    if (part.matches("-?\\d+(\\.\\d+)?")) {
                        System.out.println("Evaluation: " + part);
                        return Double.parseDouble(part); // Prende la parte numerica
                    }
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Errore di formattazione: " + e.getMessage());
            // Gestione dell'errore, ad esempio restituire un valore di default o lanciare un'eccezione
            return 0.0;
        }
        // Se non si trova un valore valido, restituisci un valore di default
        System.out.println("Nessuna valutazione trovata.");
        return 0.0;
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

        // Crea la scacchiera e l'EvalBar qui
        chessBoardpvc = new ChessBoardUI(boardStyle, pieceStyle);
        evalBarPvC = new EvalBar(20, 400);

        // Aggiungi la scacchiera e l'EvalBar al layout
        scacchiera.getChildren().clear(); // Pulisci prima il contenitore
        scacchiera.getChildren().addAll(chessBoardpvc, evalBarPvC);

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
                    gameButton.getStyleClass().add("archivebuttons"); // Aggiungi la classe CSS
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
        reviewChessBoard = new ChessBoardUI(boardStyle, pieceStyle);
        reviewEvalBar = new EvalBar(20, 400); // Initialize the EvalBar

        reviewHbox.getChildren().add(reviewChessBoard);
        reviewHbox.getChildren().add(reviewEvalBar); // Add the EvalBar to the VBox
        reviewChessBoard.loadPgn(pgn);

        // Evaluate the current position and update the EvalBar
        handleMoveUpdate();
    }

    @FXML
    private void backtoarchive() {
        Archive.setVisible(true);
        GameReviewpage.setVisible(false);
        evalscorereview.setText("");
        move1labelreview.setText("");
        move2labelreview.setText("");
        move3labelreview.setText("");

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
            reviewChessBoard.clearArrows();
            reviewChessBoard.previousMove();
            handleMoveUpdate(); // Update the EvalBar after moving
        }
    }

    @FXML
    private void nextMove() {
        if (reviewChessBoard != null) {
            reviewChessBoard.clearArrows();
            reviewChessBoard.nextMove();
            handleMoveUpdate(); // Update the EvalBar after moving
        }
    }

    private void handleMoveUpdate() {
        if (debounceHandle != null && !debounceHandle.isDone()) {
            debounceHandle.cancel(false);
        }

        debounceHandle = scheduler.schedule(() -> {
            if (reviewChessBoard != null && reviewEvalBar != null) {
                String currentFen = reviewChessBoard.getFen();

                // Cancel any ongoing Stockfish calculation
                if (currentTask != null && !currentTask.isDone()) {
                    currentTask.cancel(true);
                    stockfish.stopCalculating();  // Stop the previous calculation
                    isCalculating.set(false);
                }

                // Start a new Stockfish calculation for the updated position
                currentTask = executorService.submit(() -> {
                    isCalculating.set(true);
                    stockfish.getTopThreeMoves(currentFen, move1labelreview, move2labelreview, move3labelreview, reviewChessBoard, evalscorereview, reviewEvalBar);
                    isCalculating.set(false);
                });
            }
        }, DEBOUNCE_DELAY, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
            stockfish.stopCalculating();
        }
        executorService.shutdown();
        scheduler.shutdown();
    }

    @FXML
    private void showThemepage(){
        themeSelection.setVisible(true);
        home.setVisible(false);
    }
    @FXML
    private void changeboard(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String buttonId = clickedButton.getId();
        // Store the buttonId in a class-level variable if needed
        boardStyle = buttonId + ".png";
    }
    @FXML
    private void backtothemes(){
        boardSelection.setVisible(false);
        themeSelection.setVisible(true);
        pieceSelection.setVisible(false);
    }
    @FXML
    private void showPieceSelection(){
        pieceSelection.setVisible(true);
        themeSelection.setVisible(false);
    }
    @FXML
    private void showBoardSelection(){
        boardSelection.setVisible(true);
        themeSelection.setVisible(false);

    }
    @FXML
    private void changepieces(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String buttonId = clickedButton.getId();
        // Remove the substring "pieces" from the button ID
        String folderName = buttonId.replace("pieces", "");
        // Store the folder name in the pieceStyle variable
        pieceStyle = folderName;
    }
}
