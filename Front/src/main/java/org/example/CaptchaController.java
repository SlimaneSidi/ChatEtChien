package org.example;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.*;

public class CaptchaController {

    @FXML private VBox mainContainer;
    @FXML private Label titleLabel;
    @FXML private Label instructionLabel;
    @FXML private GridPane imageGrid;
    @FXML private Button verifyBtn;
    @FXML private Label statusLabel;
    @FXML private Label attemptsLabel;
    @FXML private ProgressBar robotBar;
    @FXML private Label robotLabel;

    // Emojis simulant des images de chats et chiens
    private static final String[] CAT_EMOJIS = {"🐱", "😺", "😸", "🐈", "😹", "😻"};
    private static final String[] DOG_EMOJIS = {"🐶", "🐕", "🦮", "🐩", "😀", "🐕‍🦺"};

    // Ce qu'on demande à l'utilisateur de cliquer
    private String targetType; // "chat" ou "chien"
    private List<CaptchaCell> cells = new ArrayList<>();
    private int attempts = 0;
    private int maxAttempts = 3;
    private boolean captchaSolved = false;

    @FXML
    public void initialize() {
        generateCaptcha();
        animateEntrance();

        // Easter egg : si l'utilisateur appuie sur Échap
        mainContainer.setOnKeyPressed(this::handleKeyPress);
    }

    // =================== GÉNÉRATION ===================

    private void generateCaptcha() {
        // Choisir aléatoirement ce qu'on demande
        targetType = Math.random() > 0.5 ? "chat" : "chien";
        updateInstruction();

        // Générer la grille 3x3
        imageGrid.getChildren().clear();
        cells.clear();

        // Créer 9 cellules avec un mélange de chats/chiens
        List<String> emojis = new ArrayList<>();

        // Garantir au moins 2-3 images de la cible
        int targetCount = 2 + (int)(Math.random() * 3);
        int otherCount = 9 - targetCount;

        String[] targetArray = targetType.equals("chat") ? CAT_EMOJIS : DOG_EMOJIS;
        String[] otherArray = targetType.equals("chat") ? DOG_EMOJIS : CAT_EMOJIS;

        for (int i = 0; i < targetCount; i++)
            emojis.add(targetArray[(int)(Math.random() * targetArray.length)] + "|target");
        for (int i = 0; i < otherCount; i++)
            emojis.add(otherArray[(int)(Math.random() * otherArray.length)] + "|other");

        // Mélanger
        Collections.shuffle(emojis);

        // Placer dans la grille
        for (int i = 0; i < 9; i++) {
            String[] parts = emojis.get(i).split("\\|");
            String emoji = parts[0];
            boolean isTarget = parts[1].equals("target");

            CaptchaCell cell = new CaptchaCell(emoji, isTarget);
            cells.add(cell);
            imageGrid.add(cell.getPane(), i % 3, i / 3);
        }
    }

    private void updateInstruction() {
        String animal = targetType.equals("chat") ? "🐱 CHATS" : "🐶 CHIENS";
        instructionLabel.setText("Cliquez sur toutes les images de " + animal);
    }

    // =================== VÉRIFICATION ===================

    @FXML
    private void handleVerify() {
        if (captchaSolved) {
            closeWindow();
            return;
        }

        attempts++;
        attemptsLabel.setText("Tentative " + attempts + "/" + maxAttempts);

        // Vérifier les sélections
        boolean allTargetsSelected = true;
        boolean noFalsePositive = true;
        int correctCount = 0;
        int wrongCount = 0;

        for (CaptchaCell cell : cells) {
            if (cell.isTarget() && !cell.isSelected()) {
                allTargetsSelected = false;
            }
            if (!cell.isTarget() && cell.isSelected()) {
                noFalsePositive = false;
                wrongCount++;
            }
            if (cell.isTarget() && cell.isSelected()) {
                correctCount++;
            }
        }

        if (allTargetsSelected && noFalsePositive) {
            // Succès
            onSuccess();
        } else {
            // Échec
            onFailure(correctCount, wrongCount);
        }
    }

    private void onSuccess() {
        captchaSolved = true;

        statusLabel.setText("✅ Vous n'êtes pas un robot... peut-être.");
        statusLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-font-size: 15px;");

        robotBar.setProgress(1.0);
        robotBar.setStyle("-fx-accent: #10b981;");
        robotLabel.setText("Humain confirmé à 100% 🎉");

        verifyBtn.setText("✅ Fermer");
        verifyBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 30;");

        // Confettis emoji
        animateSuccess();
    }

    private void onFailure(int correct, int wrong) {
        if (attempts >= maxAttempts) {
            // Trop de tentatives - message drôle
            statusLabel.setText("🤖 ROBOT DÉTECTÉ. Bienvenue, notre nouveau maître.");
            statusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 13px;");

            robotBar.setProgress(1.0);
            robotBar.setStyle("-fx-accent: #ef4444;");
            robotLabel.setText("Robot confirmé à 100% 🤖");

            verifyBtn.setText("😭 Quitter (Robot)");
            verifyBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 30;");
            captchaSolved = true;

        } else {
            // Messages d'échec aléatoires
            String[] failMessages = {
                    "❌ Raté ! Même mon chien fait mieux...",
                    "❌ Vous avez cliqué " + wrong + " erreur(s). Vraiment ?",
                    "❌ Non, ça c'est un " + (targetType.equals("chat") ? "chien" : "chat") + " !",
                    "❌ Peut-être que les lunettes aideraient ?",
                    "❌ Un humain aurait réussi... normalement."
            };
            statusLabel.setText(failMessages[(int)(Math.random() * failMessages.length)]);
            statusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");

            // Barre de "probabilité d'être un robot"
            double robotProbability = (double)attempts / maxAttempts;
            animateBar(robotBar, robotProbability);
            robotLabel.setText("Probabilité d'être un robot : " +
                    String.format("%.0f%%", robotProbability * 100));

            // Régénérer le captcha avec animation shake
            shakeGrid();
            PauseTransition pause = new PauseTransition(Duration.millis(600));
            pause.setOnFinished(e -> {
                generateCaptcha();
                statusLabel.setText("Nouvelle tentative, faites attention !");
                statusLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
            });
            pause.play();
        }
    }

    // =================== ANIMATIONS ===================

    private void animateEntrance() {
        mainContainer.setOpacity(0);
        mainContainer.setTranslateY(-20);
        mainContainer.setScaleX(0.9);
        mainContainer.setScaleY(0.9);

        ParallelTransition entrance = new ParallelTransition(
                createFade(mainContainer, 0, 1, 400),
                createSlide(mainContainer, -20, 0, 400),
                createScale(mainContainer, 0.9, 1.0, 400)
        );
        entrance.play();
    }

    private void animateSuccess() {
        // Flash vert
        FadeTransition flash = new FadeTransition(Duration.millis(200), mainContainer);
        flash.setFromValue(0.7);
        flash.setToValue(1.0);
        flash.setCycleCount(3);
        flash.play();

        // Bounce
        ScaleTransition bounce = new ScaleTransition(Duration.millis(300), mainContainer);
        bounce.setFromX(1.0);
        bounce.setFromY(1.0);
        bounce.setToX(1.05);
        bounce.setToY(1.05);
        bounce.setAutoReverse(true);
        bounce.setCycleCount(2);
        bounce.play();
    }

    private void shakeGrid() {
        TranslateTransition shake = new TranslateTransition(Duration.millis(80), imageGrid);
        shake.setFromX(0);
        shake.setToX(12);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }

    private void animateBar(ProgressBar bar, double target) {
        Timeline tl = new Timeline(new KeyFrame(Duration.millis(500),
                new KeyValue(bar.progressProperty(), target, Interpolator.EASE_BOTH)));
        tl.play();
    }

    private FadeTransition createFade(javafx.scene.Node node, double from, double to, int ms) {
        FadeTransition ft = new FadeTransition(Duration.millis(ms), node);
        ft.setFromValue(from);
        ft.setToValue(to);
        return ft;
    }

    private TranslateTransition createSlide(javafx.scene.Node node, double from, double to, int ms) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(ms), node);
        tt.setFromY(from);
        tt.setToY(to);
        return tt;
    }

    private ScaleTransition createScale(javafx.scene.Node node, double from, double to, int ms) {
        ScaleTransition st = new ScaleTransition(Duration.millis(ms), node);
        st.setFromX(from);
        st.setFromY(from);
        st.setToX(to);
        st.setToY(to);
        return st;
    }

    // =================== EASTER EGGS ===================

    private void handleKeyPress(KeyEvent event) {
        // Échap = message drôle
        if (event.getCode() == KeyCode.ESCAPE) {
            statusLabel.setText("😏 On ne fuit pas un CAPTCHA comme ça !");
            statusLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
        }
        // Entrée = soumettre
        if (event.getCode() == KeyCode.ENTER) {
            handleVerify();
        }
    }

    // =================== FERMETURE ===================

    private void closeWindow() {
        Stage stage = (Stage) mainContainer.getScene().getWindow();

        FadeTransition ft = new FadeTransition(Duration.millis(300), mainContainer);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setOnFinished(e -> stage.close());
        ft.play();
    }

    // =================== CLASSE INTERNE CELLULE ===================

    public static class CaptchaCell {
        private final StackPane pane;
        private final Label emojiLabel;
        private final boolean isTarget;
        private boolean selected = false;

        public CaptchaCell(String emoji, boolean isTarget) {
            this.isTarget = isTarget;

            // Emoji label
            emojiLabel = new Label(emoji);
            emojiLabel.setStyle("-fx-font-size: 40px;");

            // Container
            pane = new StackPane(emojiLabel);
            pane.setPrefSize(100, 100);
            pane.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-border-color: #dde6ff;" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 8;" +
                            "-fx-background-radius: 8;" +
                            "-fx-cursor: hand;"
            );

            // Hover
            pane.setOnMouseEntered(e -> {
                if (!selected) {
                    pane.setStyle(
                            "-fx-background-color: #f0f4ff;" +
                                    "-fx-border-color: #4a72b8;" +
                                    "-fx-border-width: 2;" +
                                    "-fx-border-radius: 8;" +
                                    "-fx-background-radius: 8;" +
                                    "-fx-cursor: hand;"
                    );
                    ScaleTransition st = new ScaleTransition(Duration.millis(150), pane);
                    st.setToX(1.08);
                    st.setToY(1.08);
                    st.play();
                }
            });

            pane.setOnMouseExited(e -> {
                if (!selected) {
                    pane.setStyle(
                            "-fx-background-color: white;" +
                                    "-fx-border-color: #dde6ff;" +
                                    "-fx-border-width: 2;" +
                                    "-fx-border-radius: 8;" +
                                    "-fx-background-radius: 8;" +
                                    "-fx-cursor: hand;"
                    );
                    ScaleTransition st = new ScaleTransition(Duration.millis(150), pane);
                    st.setToX(1.0);
                    st.setToY(1.0);
                    st.play();
                }
            });

            // Clic
            pane.setOnMouseClicked(e -> toggleSelection());
        }

        private void toggleSelection() {
            selected = !selected;

            if (selected) {
                pane.setStyle(
                        "-fx-background-color: #dde6ff;" +
                                "-fx-border-color: #001f65;" +
                                "-fx-border-width: 3;" +
                                "-fx-border-radius: 8;" +
                                "-fx-background-radius: 8;" +
                                "-fx-cursor: hand;"
                );

                // Checkmark
                Label check = new Label("✓");
                check.setStyle(
                        "-fx-font-size: 20px;" +
                                "-fx-text-fill: #001f65;" +
                                "-fx-font-weight: bold;"
                );
                check.setTranslateX(35);
                check.setTranslateY(-35);
                pane.getChildren().add(check);

                // Animation
                ScaleTransition st = new ScaleTransition(Duration.millis(200), pane);
                st.setFromX(1.0);
                st.setFromY(1.0);
                st.setToX(0.95);
                st.setToY(0.95);
                st.setAutoReverse(true);
                st.setCycleCount(2);
                st.play();

            } else {
                pane.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-border-color: #dde6ff;" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 8;" +
                                "-fx-background-radius: 8;" +
                                "-fx-cursor: hand;"
                );
                // Supprimer le checkmark
                pane.getChildren().removeIf(n -> n instanceof Label && ((Label)n).getText().equals("✓"));
            }
        }

        public StackPane getPane() { return pane; }
        public boolean isTarget() { return isTarget; }
        public boolean isSelected() { return selected; }
    }
}