package org.example;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.File;
import java.util.Random;

public class Controller {

    // Drop zone
    @FXML private StackPane dropZone;
    @FXML private VBox dropHint;
    @FXML private ImageView previewImage;

    // Infos image
    @FXML private HBox imageInfoBox;
    @FXML private Label imageNameLabel;
    @FXML private Label imageSizeLabel;
    @FXML private Label imageWeightLabel;

    // Boutons
    @FXML private Button analyzeBtn;
    @FXML private Button clearBtn;

    // Résultat principal
    @FXML private VBox resultPanel;
    @FXML private VBox resultPlaceholder;
    @FXML private Label resultEmoji;
    @FXML private Label resultLabel;
    @FXML private ProgressBar confidenceBar;
    @FXML private Label confidencePercent;

    // Barres chat/chien
    @FXML private ProgressBar catBar;
    @FXML private ProgressBar dogBar;
    @FXML private Label catPercent;
    @FXML private Label dogPercent;

    // Stats session
    @FXML private Label totalAnalyses;
    @FXML private Label totalChats;
    @FXML private Label totalChiens;
    @FXML private Label avgConfidence;

    // Logs
    @FXML private TextArea logArea;

    // Variables internes
    private File selectedImageFile;
    private int sessionTotal = 0;
    private int sessionChats = 0;
    private int sessionChiens = 0;
    private double sessionConfidenceSum = 0;

    @FXML
    public void initialize() {
        setupDragAndDrop();
        resetUI();
        addLog("✓ Application prête");
        addLog("✓ Neurone chargé et opérationnel");
        addLog("→ Glissez une image pour commencer");
        addLog("💡 Astuce : Cliquez sur le bouton CAPTCHA 🔒");

        javafx.application.Platform.runLater(() -> {
            try {
                Scene scene = dropZone.getScene();
                if (scene != null) {
                    // Ajouter le listener sur la scène
                    scene.setOnKeyPressed(event -> {
                        if (event.getCode() == KeyCode.C) {
                            System.out.println("C pressé !");
                            openCaptcha();
                        }
                    });
                    System.out.println("✓ Listener clavier installé sur la scène");
                }
            } catch (Exception e) {
                System.err.println("Erreur setup raccourci : " + e.getMessage());
            }
        });
    }

    private void openCaptcha() {
        try {
            // Vérifier que la scène est prête
            if (dropZone.getScene() == null) {
                System.err.println("Scène non disponible !");
                return;
            }

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/frontend/captcha.fxml")
            );
            Parent root = loader.load();

            Stage captchaStage = new Stage();
            captchaStage.initModality(Modality.APPLICATION_MODAL);
            captchaStage.initStyle(StageStyle.TRANSPARENT);

            Stage mainStage = (Stage) dropZone.getScene().getWindow();
            captchaStage.initOwner(mainStage);

            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            scene.getStylesheets().add(
                    getClass().getResource("/frontend/style.css").toExternalForm()
            );

            captchaStage.setScene(scene);
            captchaStage.setAlwaysOnTop(true);
            captchaStage.show();

            // Centrer sur la fenêtre principale
            captchaStage.setX(mainStage.getX() +
                    (mainStage.getWidth() - captchaStage.getWidth()) / 2);
            captchaStage.setY(mainStage.getY() +
                    (mainStage.getHeight() - captchaStage.getScene().getHeight()) / 2);

            addLog("🔒 CAPTCHA lancé... Prouvez que vous n'êtes pas un robot 😄");

        } catch (Exception e) {
            System.err.println("Erreur ouverture CAPTCHA : " + e.getMessage());
            e.printStackTrace();
            addLog("✗ Erreur ouverture CAPTCHA");
        }
    }

    @FXML
    private void handleOpenCaptcha() {
        System.out.println("Ouverture du CAPTCHA via bouton...");
        openCaptcha();
    }

    // =================== DRAG AND DROP ===================

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
            dropZone.getStyleClass().remove("drop-zone-active");
        });

        dropZone.setOnMouseClicked(event -> handleBrowseImage());
    }

    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                name.endsWith(".png") || name.endsWith(".bmp");
    }

    @FXML
    private void handleBrowseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Sélectionner une image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.bmp")
        );
        File file = chooser.showOpenDialog(dropZone.getScene().getWindow());
        if (file != null) loadImage(file);
    }

    private void loadImage(File file) {
        try {
            selectedImageFile = file;
            Image image = new Image(file.toURI().toString());

            // Afficher l'image
            previewImage.setImage(image);
            previewImage.setVisible(true);
            dropHint.setVisible(false);
            dropHint.setManaged(false);

            // Infos image
            imageNameLabel.setText("📄 " + file.getName());
            imageSizeLabel.setText("📐 " + (int)image.getWidth() + "×" + (int)image.getHeight());
            imageWeightLabel.setText("💾 " + (file.length() / 1024) + " Ko");
            imageInfoBox.setVisible(true);
            imageInfoBox.setManaged(true);

            // Activer boutons
            analyzeBtn.setDisable(false);
            clearBtn.setDisable(false);

            // Réinitialiser résultat
            resultPanel.setVisible(false);
            resultPanel.setManaged(false);
            resultPlaceholder.setVisible(true);
            resultPlaceholder.setManaged(true);

            // Animation d'apparition de l'image
            FadeTransition ft = new FadeTransition(Duration.millis(400), previewImage);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();

            addLog("✓ Image chargée : " + file.getName());
            addLog("  Dimensions : " + (int)image.getWidth() + "×" + (int)image.getHeight() + " px");
            addLog("  Taille : " + (file.length() / 1024) + " Ko");

        } catch (Exception e) {
            addLog("✗ Erreur : " + e.getMessage());
        }
    }

    // =================== ANALYSE ===================

    @FXML
    private void handleAnalyze() {
        if (selectedImageFile == null) return;

        analyzeBtn.setDisable(true);
        clearBtn.setDisable(true);
        addLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        addLog("🔍 Analyse en cours...");

        simulateAnalysis();
    }

    private void simulateAnalysis() {
        new Thread(() -> {
            try {
                // Étapes de traitement
                sleep(300);
                log("  → Conversion niveaux de gris...");
                sleep(400);
                log("  → Normalisation [0, 1]...");
                sleep(400);
                log("  → Passage dans le neurone...");
                sleep(600);
                log("  → Calcul des probabilités...");
                sleep(300);

                // Résultat simulé
                Random rand = new Random();
                boolean isChat = rand.nextBoolean();
                double confidence = 0.62 + rand.nextDouble() * 0.35;
                double catConf = isChat ? confidence : (1 - confidence);
                double dogConf = isChat ? (1 - confidence) : confidence;

                // Mettre à jour UI
                javafx.application.Platform.runLater(() -> {
                    displayResult(isChat, confidence, catConf, dogConf);
                    analyzeBtn.setDisable(false);
                    clearBtn.setDisable(false);
                });

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void displayResult(boolean isChat, double confidence, double catConf, double dogConf) {
        // Mettre à jour le résultat
        String emoji = isChat ? "🐱" : "🐶";
        String animal = isChat ? "CHAT" : "CHIEN";
        String color = isChat ? "#4a72b8" : "#1a3a8c";

        resultEmoji.setText(emoji);
        resultLabel.setText(animal);
        resultLabel.setStyle("-fx-text-fill: " + color + ";");

        // Animation des barres
        animateBar(confidenceBar, confidence);
        animateBar(catBar, catConf);
        animateBar(dogBar, dogConf);

        confidencePercent.setText(String.format("%.1f%%", confidence * 100));
        catPercent.setText(String.format("%.1f%%", catConf * 100));
        dogPercent.setText(String.format("%.1f%%", dogConf * 100));

        // Afficher le panel résultat avec animation
        resultPlaceholder.setVisible(false);
        resultPlaceholder.setManaged(false);
        resultPanel.setVisible(true);
        resultPanel.setManaged(true);

        FadeTransition ft = new FadeTransition(Duration.millis(500), resultPanel);
        ft.setFromValue(0);
        ft.setToValue(1);

        ScaleTransition st = new ScaleTransition(Duration.millis(500), resultPanel);
        st.setFromX(0.8);
        st.setFromY(0.8);
        st.setToX(1.0);
        st.setToY(1.0);

        new ParallelTransition(ft, st).play();

        // Mettre à jour les stats session
        sessionTotal++;
        sessionConfidenceSum += confidence;
        if (isChat) sessionChats++;
        else sessionChiens++;

        totalAnalyses.setText(String.valueOf(sessionTotal));
        totalChats.setText(String.valueOf(sessionChats));
        totalChiens.setText(String.valueOf(sessionChiens));
        avgConfidence.setText(String.format("%.0f%%", (sessionConfidenceSum / sessionTotal) * 100));

        // Log résultat
        addLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        addLog("✅ Résultat : " + emoji + " " + animal);
        addLog("   Confiance : " + String.format("%.1f%%", confidence * 100));
        addLog("   Chat : " + String.format("%.1f%%", catConf * 100));
        addLog("   Chien : " + String.format("%.1f%%", dogConf * 100));
    }

    private void animateBar(ProgressBar bar, double target) {
        Timeline timeline = new Timeline();
        double current = bar.getProgress();
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(600),
                        new KeyValue(bar.progressProperty(), target, Interpolator.EASE_BOTH))
        );
        timeline.play();
    }

    // =================== ACTIONS ===================

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

        addLog("🗑 Image effacée - Prêt");
    }

    @FXML
    private void handleClearLog() {
        logArea.clear();
    }

    @FXML
    private void handleBack() {
        MainApp.showWelcomePage();
    }

    // =================== UI ===================

    private void resetUI() {
        analyzeBtn.setDisable(true);
        clearBtn.setDisable(true);
        resultPanel.setVisible(false);
        resultPanel.setManaged(false);
        imageInfoBox.setVisible(false);
        imageInfoBox.setManaged(false);
    }

    // Animation de secousse pour erreur
    private void shakeNode(javafx.scene.Node node) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(60), node);
        shake.setFromX(0);
        shake.setToX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }

    // =================== UTILS ===================

    private void addLog(String message) {
        String time = java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        javafx.application.Platform.runLater(() ->
                logArea.appendText("[" + time + "] " + message + "\n")
        );
    }

    private void log(String message) {
        javafx.application.Platform.runLater(() -> addLog(message));
    }

    private void sleep(int ms) throws InterruptedException {
        Thread.sleep(ms);
    }
}

