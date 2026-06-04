package org.example;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class CaptchaController {

    // ── FXML ──────────────────────────────────────────────────────────────
    @FXML private VBox mainContainer;
    @FXML private Label titleLabel, instructionLabel, statusLabel, attemptsLabel, robotLabel;
    @FXML private GridPane imageGrid;
    @FXML private Button verifyBtn;
    @FXML private ProgressBar robotBar;

    // ── Configuration ─────────────────────────────────────────────────────
    private static final String DATASET_DIR = "src/main/dataset_groupe_9/test";
    private static final int MAX_ATTEMPTS = 3;

    // ── Données en cache ──────────────────────────────────────────────────
    private static final List<String> catPaths = new ArrayList<>();
    private static final List<String> dogPaths = new ArrayList<>();
    private static final List<String> wildPaths = new ArrayList<>();
    private static boolean isDatasetLoaded = false;

    // ── État de la partie ─────────────────────────────────────────────────
    private String targetType;
    private int attempts = 0;
    private boolean isSolved = false;
    private final List<CaptchaCell> cells = new ArrayList<>();

    // ══════════════════════════════════════════════════════════════════════
    //  INITIALISATION
    // ══════════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        mainContainer.setFocusTraversable(true);
        mainContainer.setOnKeyPressed(this::handleKeyPress);

        animateEntrance();
        loadDatasetPaths();
        generateGrid();
    }

    private void loadDatasetPaths() {
        if (isDatasetLoaded) return;

        File root = new File(DATASET_DIR);
        if (root.exists()) {
            scanDirectory(root);
        } else {
            System.err.println("⚠️ Dataset introuvable : " + root.getAbsolutePath());
        }
        isDatasetLoaded = true;
    }

    private void scanDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.isDirectory()) {
                scanDirectory(f);
            } else {
                String path = f.getAbsolutePath();
                String lower = path.toLowerCase();
                if (!lower.endsWith(".jpg") && !lower.endsWith(".png") && !lower.endsWith(".jpeg")) continue;

                String folderName = f.getParentFile().getName().toLowerCase();

                if (folderName.contains("cat") || folderName.contains("chat")) {
                    catPaths.add(path);
                } else if (folderName.contains("dog") || folderName.contains("chien")) {
                    dogPaths.add(path);
                } else {
                    wildPaths.add(path);
                }
            }
        }
    }

    // Fonction utilitaire pour retrouver la catégorie stricte d'une image
    private String getCategory(String path) {
        if (catPaths.contains(path)) return "chat";
        if (dogPaths.contains(path)) return "chien";
        return "wild";
    }

    // ══════════════════════════════════════════════════════════════════════
    //  GÉNÉRATION DU CAPTCHA
    // ══════════════════════════════════════════════════════════════════════

    private void generateGrid() {
        imageGrid.getChildren().clear();
        cells.clear();
        statusLabel.setText("");

        String[] types = {"chat", "chien", "wild"};
        targetType = types[new Random().nextInt(3)];

        String texteCible = switch (targetType) {
            case "chat" -> "🐱 CHATS";
            case "chien" -> "🐶 CHIENS";
            default -> "🦁 ANIMAUX SAUVAGES";
        };
        instructionLabel.setText("Cliquez sur toutes les images de " + texteCible);

        List<String> poolCible = new ArrayList<>();
        List<String> poolAutres = new ArrayList<>();

        if (targetType.equals("chat")) {
            poolCible.addAll(catPaths);
            poolAutres.addAll(dogPaths);
            poolAutres.addAll(wildPaths);
        } else if (targetType.equals("chien")) {
            poolCible.addAll(dogPaths);
            poolAutres.addAll(catPaths);
            poolAutres.addAll(wildPaths);
        } else {
            poolCible.addAll(wildPaths);
            poolAutres.addAll(catPaths);
            poolAutres.addAll(dogPaths);
        }

        Random rnd = new Random();
        int targetCount = 3 + rnd.nextInt(2);
        int otherCount = 9 - targetCount;

        List<CellData> gridData = new ArrayList<>();

        List<String> pickedTargets = pickDistinctRandom(poolCible, targetCount, rnd);
        for (String path : pickedTargets) {
            gridData.add(new CellData(targetType, path));
        }

        List<String> pickedOthers = pickDistinctRandom(poolAutres, otherCount, rnd);
        for (String path : pickedOthers) {
            gridData.add(new CellData(getCategory(path), path));
        }

        Collections.shuffle(gridData, rnd);

        for (int i = 0; i < gridData.size(); i++) {
            CaptchaCell cell = new CaptchaCell(gridData.get(i));
            cells.add(cell);
            imageGrid.add(cell.getPane(), i % 3, i / 3);
        }
    }

    private List<String> pickDistinctRandom(List<String> pool, int count, Random rnd) {
        List<String> result = new ArrayList<>();
        if (pool == null || pool.isEmpty()) return result;

        List<String> copy = new ArrayList<>(pool);
        for (int i = 0; i < count && !copy.isEmpty(); i++) {
            result.add(copy.remove(rnd.nextInt(copy.size())));
        }
        return result;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ACTIONS ET VÉRIFICATION
    // ══════════════════════════════════════════════════════════════════════

    @FXML
    private void handleVerify() {
        if (isSolved) { closeWindow(); return; }

        attempts++;
        attemptsLabel.setText(attempts + " / " + MAX_ATTEMPTS + " tentatives");

        boolean success = true;
        int wrongClicks = 0;

        for (CaptchaCell cell : cells) {
            boolean isTarget = cell.trueType.equals(targetType);
            if (isTarget && !cell.isSelected) success = false;
            if (!isTarget && cell.isSelected) { success = false; wrongClicks++; }
        }

        if (success) {
            handleSuccess();
        } else {
            handleFailure(wrongClicks);
        }
    }

    private void handleSuccess() {
        isSolved = true;
        statusLabel.setText("✅ Humain confirmé !");
        statusLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");

        robotBar.setProgress(1.0);
        robotBar.setStyle("-fx-accent: #10b981;");
        robotLabel.setText("Humain confirmé à 100% 🎉");

        verifyBtn.setText("✅ Fermer");
        verifyBtn.setStyle("-fx-background-color: #10b981;");

        ScaleTransition st = new ScaleTransition(Duration.millis(300), mainContainer);
        st.setFromX(1.0); st.setToX(1.05);
        st.setFromY(1.0); st.setToY(1.05);
        st.setAutoReverse(true); st.setCycleCount(2);
        st.play();
    }

    private void handleFailure(int wrongClicks) {
        if (attempts >= MAX_ATTEMPTS) {
            statusLabel.setText("🤖 ROBOT DÉTECTÉ.");
            statusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
            robotBar.setProgress(1.0);
            robotBar.setStyle("-fx-accent: #ef4444;");
            verifyBtn.setText("Quitter (Robot)");
            verifyBtn.setStyle("-fx-background-color: #ef4444;");
            isSolved = true;
        } else {
            statusLabel.setText("❌ " + wrongClicks + " erreur(s). Réessayez !");
            statusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");

            robotBar.setProgress((double) attempts / MAX_ATTEMPTS);

            TranslateTransition tt = new TranslateTransition(Duration.millis(50), imageGrid);
            tt.setFromX(-10); tt.setToX(10);
            tt.setCycleCount(6); tt.setAutoReverse(true);
            tt.setOnFinished(e -> generateGrid());
            tt.play();
        }
    }

    @FXML private void handleRefresh() { generateGrid(); }
    @FXML private void handleClose() { closeWindow(); }
    @FXML private void handleBackdropClick() { closeWindow(); }

    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) closeWindow();
        if (event.getCode() == KeyCode.ENTER) handleVerify();
    }

    private void closeWindow() {
        Stage stage = (Stage) mainContainer.getScene().getWindow();
        stage.close();
    }

    private void animateEntrance() {
        ScaleTransition st = new ScaleTransition(Duration.millis(300), mainContainer);
        st.setFromX(0.8); st.setToX(1.0);
        st.setFromY(0.8); st.setToY(1.0);
        st.play();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  CLASSE INTERNE : CELLULE DU CAPTCHA
    // ══════════════════════════════════════════════════════════════════════

    private record CellData(String trueType, String path) {}

    private class CaptchaCell {
        private final StackPane pane;
        private final String trueType;
        private boolean isSelected = false;
        private final Label checkmark;

        public CaptchaCell(CellData data) {
            this.trueType = data.trueType();
            this.pane = new StackPane();
            this.pane.setPrefSize(108, 108);
            this.pane.setStyle("-fx-background-color: white; -fx-border-color: #dde6ff; -fx-border-width: 2; -fx-border-radius: 8; -fx-cursor: hand;");

            ImageView imgView = new ImageView();
            imgView.setFitWidth(96);
            imgView.setFitHeight(96);
            if (data.path() != null) {
                imgView.setImage(new Image(new File(data.path()).toURI().toString(), 96, 96, false, true, false));
            }

            checkmark = new Label("✓");
            checkmark.setStyle("-fx-font-size: 20px; -fx-text-fill: #001f65; -fx-font-weight: bold; -fx-background-color: rgba(255,255,255,0.85); -fx-background-radius: 50; -fx-padding: 2 6;");
            StackPane.setAlignment(checkmark, Pos.TOP_RIGHT);
            checkmark.setVisible(false);

            pane.getChildren().addAll(imgView, checkmark);
            pane.setOnMouseClicked(e -> toggleSelection());
        }

        private void toggleSelection() {
            isSelected = !isSelected;
            checkmark.setVisible(isSelected);
            pane.setStyle(isSelected
                    ? "-fx-border-color: #001f65; -fx-border-width: 3; -fx-border-radius: 8; -fx-cursor: hand;"
                    : "-fx-background-color: white; -fx-border-color: #dde6ff; -fx-border-width: 2; -fx-border-radius: 8; -fx-cursor: hand;");

            ScaleTransition st = new ScaleTransition(Duration.millis(100), pane);
            st.setToX(isSelected ? 0.95 : 1.0);
            st.setToY(isSelected ? 0.95 : 1.0);
            st.play();
        }

        public StackPane getPane() { return pane; }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  MÉTHODES PUBLIQUES POUR LE MINI-JEU SWING (Remises comme à l'origine)
    // ══════════════════════════════════════════════════════════════════════

    public static List<String> collectAllImages(String path) {
        List<String> images = new ArrayList<>();
        File root = new File(path);
        if (!root.exists()) return images;
        scanDirFlat(root, images);
        return images;
    }

    private static void scanDirFlat(File dir, List<String> images) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) scanDirFlat(f, images);
            else {
                String lower = f.getName().toLowerCase();
                if (lower.endsWith(".jpg") || lower.endsWith(".png") || lower.endsWith(".jpeg") || lower.endsWith(".bmp")) {
                    images.add(f.getAbsolutePath());
                }
            }
        }
    }
}