package org.example;

import java.io.File;
import java.util.List;

import org.example.neurone.iNeurone;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class Controller {

    // ── Drop zone ──────────────────────────────────────────────────────────
    @FXML private StackPane dropZone;
    @FXML private VBox      dropHint;
    @FXML private ImageView previewImage;

    // ── Infos image ────────────────────────────────────────────────────────
    @FXML private HBox  imageInfoBox;
    @FXML private Label imageNameLabel;
    @FXML private Label imageSizeLabel;
    @FXML private Label imageWeightLabel;

    // ── Boutons ────────────────────────────────────────────────────────────
    @FXML private Button analyzeBtn;
    @FXML private Button clearBtn;

    // ── Résultat principal ─────────────────────────────────────────────────
    @FXML private VBox        resultPanel;
    @FXML private VBox        resultPlaceholder;
    @FXML private Label       resultEmoji;
    @FXML private Label       resultLabel;
    @FXML private Label       lowConfidenceLabel;
    @FXML private ProgressBar confidenceBar;
    @FXML private Label       confidencePercent;

    // ── Barres chat / chien / wild ─────────────────────────────────────────
    @FXML private ProgressBar catBar;
    @FXML private ProgressBar dogBar;
    @FXML private ProgressBar wildBar;
    @FXML private Label       catPercent;
    @FXML private Label       dogPercent;
    @FXML private Label       wildPercent;

    // ── Stats session ──────────────────────────────────────────────────────
    @FXML private Label totalAnalyses;
    @FXML private Label totalChats;
    @FXML private Label totalChiens;
    @FXML private Label totalWild;
    @FXML private Label avgConfidence;

    // ── Logs ───────────────────────────────────────────────────────────────
    @FXML private TextArea logArea;

    // ── Variables internes ─────────────────────────────────────────────────
    private File   selectedImageFile;
    private int    sessionTotal  = 0;
    private int    sessionChats  = 0;
    private int    sessionChiens = 0;
    private int    sessionWild   = 0;
    private double sessionConfidenceSum = 0;

    private static final double SEUIL_CONFIANCE = 0.55;

    private static final String COLOR_CHAT    = "#4a72b8";
    private static final String COLOR_CHIEN   = "#1a3a8c";
    private static final String COLOR_WILD    = "#7c5c2e";
    private static final String COLOR_INCONNU = "#6b7280";

    // ══════════════════════════════════════════════════════════════════════
    //  INITIALISATION
    // ══════════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        setupDragAndDrop();
        resetUI();

        // Chargement du moteur IA (singleton — utilise NeuroneSigmoide + Hog du back)
        AIEngine engine = AIEngine.getInstance();
        if (engine.isLoaded()) {
            addLog("✅ Moteur IA chargé et prêt");
        } else {
            addLog("⚠️  Mode démo — neurones non chargés");
        }
        addLog("→ Glissez une image pour commencer");
        addLog("💡 Appuyez sur [C] ou cliquez sur 🔒 CAPTCHA");

        // Raccourci clavier C → CAPTCHA
        javafx.application.Platform.runLater(() -> {
            try {
                Scene scene = dropZone.getScene();
                if (scene != null) {
                    scene.setOnKeyPressed(event -> {
                        if (event.getCode() == KeyCode.C) openCaptcha();
                    });
                }
            } catch (Exception e) {
                System.err.println("Erreur raccourci : " + e.getMessage());
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════════
    //  MINI-JEU
    //  Lance UserInterface.java avec les neurones du singleton AIEngine
    // ══════════════════════════════════════════════════════════════════════

    @FXML
    private void handleMiniGame() {
        try {
            addLog("🎮 Lancement du mini-jeu...");

            List<String> cheminsTest = CaptchaController.collectAllImages(
                    "src/main/dataset_groupe_9/test");

            if (cheminsTest.isEmpty()) {
                addLog("⚠️ Aucune image trouvée pour le mini-jeu.");
                return;
            }

            iNeurone[] neurones = AIEngine.getInstance().getNeurones();

            UserInterface.start(cheminsTest, neurones);

        } catch (Exception e) {
            addLog("❌ Erreur mini-jeu : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  CAPTCHA
    // ══════════════════════════════════════════════════════════════════════

    private void openCaptcha() {
        try {
            if (dropZone.getScene() == null) return;

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/frontend/captcha.fxml"));
            Parent root = loader.load();

            Stage captchaStage = new Stage();
            captchaStage.initModality(Modality.APPLICATION_MODAL);
            captchaStage.initStyle(StageStyle.TRANSPARENT);

            Stage mainStage = (Stage) dropZone.getScene().getWindow();
            captchaStage.initOwner(mainStage);

            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            scene.getStylesheets().add(
                    getClass().getResource("/frontend/style.css").toExternalForm());

            captchaStage.setScene(scene);
            captchaStage.setAlwaysOnTop(true);
            captchaStage.show();

            captchaStage.setX(mainStage.getX()
                    + (mainStage.getWidth()  - captchaStage.getWidth())  / 2);
            captchaStage.setY(mainStage.getY()
                    + (mainStage.getHeight() - captchaStage.getScene().getHeight()) / 2);

            addLog("🔒 CAPTCHA ouvert !");

        } catch (Exception e) {
            addLog("✗ Erreur CAPTCHA : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML private void handleOpenCaptcha() { openCaptcha(); }

    // ══════════════════════════════════════════════════════════════════════
    //  DRAG AND DROP
    // ══════════════════════════════════════════════════════════════════════

    private void setupDragAndDrop() {
        dropZone.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
                dropZone.getStyleClass().add("drop-zone-active");
            }
            event.consume();
        });

        dropZone.setOnDragExited(event -> {
            dropZone.getStyleClass().remove("drop-zone-active");
            event.consume();
        });

        dropZone.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                File file = db.getFiles().get(0);
                if (isImageFile(file)) {
                    loadImage(file);
                    success = true;
                } else {
                    shakeNode(dropZone);
                    addLog("✗ Format non supporté : " + file.getName());
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        dropZone.setOnMouseClicked(event -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Choisir une image");
            fc.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.bmp")
            );
            File file = fc.showOpenDialog(dropZone.getScene().getWindow());
            if (file != null) loadImage(file);
        });
    }

    private boolean isImageFile(File f) {
        String name = f.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg")
                || name.endsWith(".png") || name.endsWith(".bmp");
    }

    private void loadImage(File file) {
        selectedImageFile = file;

        // Aperçu
        Image img = new Image(file.toURI().toString());
        previewImage.setImage(img);
        previewImage.setVisible(true);
        dropHint.setVisible(false);
        dropHint.setManaged(false);

        // Infos
        imageNameLabel.setText(file.getName());
        imageSizeLabel.setText((int) img.getWidth() + " × " + (int) img.getHeight() + " px");
        imageWeightLabel.setText(String.format("%.1f Ko", file.length() / 1024.0));
        imageInfoBox.setVisible(true);
        imageInfoBox.setManaged(true);

        analyzeBtn.setDisable(false);
        clearBtn.setDisable(false);

        addLog("📂 Image chargée : " + file.getName());
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ANALYSE
    // ══════════════════════════════════════════════════════════════════════

    @FXML
    private void handleAnalyze() {
        if (selectedImageFile == null) return;

        analyzeBtn.setDisable(true);
        clearBtn.setDisable(true);
        addLog("🔍 Analyse en cours...");

        new Thread(() -> {
            try {
                // Prédiction via AIEngine → NeuroneSigmoide + Hog du back
                AIEngine.PredictResult result = AIEngine.getInstance().predict(selectedImageFile);

                javafx.application.Platform.runLater(() -> {
                    displayResult(result);
                    analyzeBtn.setDisable(false);
                    clearBtn.setDisable(false);
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    addLog("✗ Erreur IA : " + e.getMessage());
                    analyzeBtn.setDisable(false);
                    clearBtn.setDisable(false);
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void displayResult(AIEngine.PredictResult result) {

        double confidence = result.score;
        boolean lowConf   = confidence < SEUIL_CONFIANCE;

        if (lowConf) {
            resultEmoji.setText("❓");
            resultLabel.setText("INDÉTERMINÉ");
            resultLabel.setStyle("-fx-text-fill: " + COLOR_INCONNU + ";");
        } else {
            resultEmoji.setText(result.emoji);
            resultLabel.setText(result.label.toUpperCase());
            String color = switch (result.type) {
                case 0  -> COLOR_CHAT;
                case 1  -> COLOR_CHIEN;
                case 2  -> COLOR_WILD;
                default -> COLOR_INCONNU;
            };
            resultLabel.setStyle("-fx-text-fill: " + color + ";");
        }

        lowConfidenceLabel.setVisible(lowConf);
        lowConfidenceLabel.setManaged(lowConf);

        animateBar(confidenceBar, confidence);
        confidencePercent.setText(String.format("%.1f%%", confidence * 100));

        double catScore  = result.scores.length > 0 ? result.scores[0] : 0;
        double dogScore  = result.scores.length > 1 ? result.scores[1] : 0;
        double wildScore = result.scores.length > 2 ? result.scores[2] : 0;

        animateBar(catBar,  catScore);
        animateBar(dogBar,  dogScore);
        animateBar(wildBar, wildScore);

        catPercent.setText(String.format("%.1f%%",  catScore  * 100));
        dogPercent.setText(String.format("%.1f%%",  dogScore  * 100));
        wildPercent.setText(String.format("%.1f%%", wildScore * 100));

        highlightWinner(result.type, lowConf);

        resultPlaceholder.setVisible(false);
        resultPlaceholder.setManaged(false);
        resultPanel.setVisible(true);
        resultPanel.setManaged(true);

        FadeTransition ft = new FadeTransition(Duration.millis(500), resultPanel);
        ft.setFromValue(0); ft.setToValue(1);
        ScaleTransition st = new ScaleTransition(Duration.millis(500), resultPanel);
        st.setFromX(0.85); st.setFromY(0.85);
        st.setToX(1.0);    st.setToY(1.0);
        new ParallelTransition(ft, st).play();

        sessionTotal++;
        sessionConfidenceSum += confidence;
        if (!lowConf) {
            switch (result.type) {
                case 0 -> sessionChats++;
                case 1 -> sessionChiens++;
                case 2 -> sessionWild++;
            }
        }
        totalAnalyses.setText(String.valueOf(sessionTotal));
        totalChats.setText(String.valueOf(sessionChats));
        totalChiens.setText(String.valueOf(sessionChiens));
        totalWild.setText(String.valueOf(sessionWild));
        avgConfidence.setText(String.format("%.0f%%",
                (sessionConfidenceSum / sessionTotal) * 100));

        addLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        if (lowConf) {
            addLog("⚠️  INDÉTERMINÉ — confiance trop faible");
        } else {
            addLog("✅ " + result.emoji + " " + result.label.toUpperCase());
        }
        addLog(String.format("   🐱 Chat  : %.1f%%", catScore  * 100));
        addLog(String.format("   🐶 Chien : %.1f%%", dogScore  * 100));
        addLog(String.format("   🦁 Wild  : %.1f%%", wildScore * 100));
        addLog(String.format("   Confiance : %.1f%%", confidence * 100));
        if (lowConf) addLog("   (Seuil : " + (int)(SEUIL_CONFIANCE * 100) + "%)");
    }

    private void highlightWinner(int winnerType, boolean lowConf) {
        catBar.setStyle("-fx-accent: #b0c4ff;");
        dogBar.setStyle("-fx-accent: #b0c4ff;");
        wildBar.setStyle("-fx-accent: #d4c4a8;");
        if (lowConf) return;
        switch (winnerType) {
            case 0 -> catBar.setStyle("-fx-accent: linear-gradient(to right, #4a72b8, #6895fd);");
            case 1 -> dogBar.setStyle("-fx-accent: linear-gradient(to right, #1a3a8c, #4a72b8);");
            case 2 -> wildBar.setStyle("-fx-accent: linear-gradient(to right, #7c5c2e, #c4a265);");
        }
    }

    private void animateBar(ProgressBar bar, double target) {
        Timeline tl = new Timeline(new KeyFrame(Duration.millis(600),
                new KeyValue(bar.progressProperty(), target, Interpolator.EASE_BOTH)));
        tl.play();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ACTIONS
    // ══════════════════════════════════════════════════════════════════════

    @FXML
    private void handleClear() {
        selectedImageFile = null;
        previewImage.setImage(null);
        previewImage.setVisible(false);
        dropHint.setVisible(true);
        dropHint.setManaged(true);
        imageInfoBox.setVisible(false);
        imageInfoBox.setManaged(false);
        resultPanel.setVisible(false);
        resultPanel.setManaged(false);
        resultPlaceholder.setVisible(true);
        resultPlaceholder.setManaged(true);
        analyzeBtn.setDisable(true);
        clearBtn.setDisable(true);

        resetBar(confidenceBar, confidencePercent);
        resetBar(catBar,  catPercent);
        resetBar(dogBar,  dogPercent);
        resetBar(wildBar, wildPercent);

        addLog("🗑 Effacé — Prêt");
    }

    private void resetBar(ProgressBar bar, Label label) {
        bar.setProgress(0);
        bar.setStyle("");
        label.setText("0%");
    }

    @FXML private void handleClearLog() { logArea.clear(); }
    @FXML private void handleBack()     { MainApp.showWelcomePage(); }

    // ══════════════════════════════════════════════════════════════════════
    //  UI
    // ══════════════════════════════════════════════════════════════════════

    private void resetUI() {
        analyzeBtn.setDisable(true);
        clearBtn.setDisable(true);
        resultPanel.setVisible(false);
        resultPanel.setManaged(false);
        imageInfoBox.setVisible(false);
        imageInfoBox.setManaged(false);
    }

    private void shakeNode(javafx.scene.Node node) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(60), node);
        shake.setFromX(0);
        shake.setToX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }

    private void addLog(String message) {
        String time = java.time.LocalTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        javafx.application.Platform.runLater(() ->
                logArea.appendText("[" + time + "] " + message + "\n"));
    }
}