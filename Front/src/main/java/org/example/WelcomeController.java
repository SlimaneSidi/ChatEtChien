package org.example;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class WelcomeController {

    @FXML private VBox mainContainer;
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label catEmoji;
    @FXML private Label dogEmoji;
    @FXML private Label startLabel;
    @FXML private Label descriptionLabel;
    @FXML private Circle circle1;
    @FXML private Circle circle2;
    @FXML private Circle circle3;
    @FXML private Circle circle4;

    @FXML
    public void initialize() {
        // Lancer les animations au démarrage
        animateEntrance();
        animateFloatingCircles();
        animatePulseStart();
        animateEmojis();
    }

    // Animation d'entrée générale
    private void animateEntrance() {
        // Container principal
        mainContainer.setOpacity(0);
        mainContainer.setTranslateY(30);

        FadeTransition fade = new FadeTransition(Duration.millis(1000), mainContainer);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(1000), mainContainer);
        slide.setFromY(30);
        slide.setToY(0);

        ParallelTransition entrance = new ParallelTransition(fade, slide);
        entrance.setDelay(Duration.millis(200));
        entrance.play();

        // Titre avec délai
        titleLabel.setOpacity(0);
        FadeTransition titleFade = new FadeTransition(Duration.millis(800), titleLabel);
        titleFade.setFromValue(0);
        titleFade.setToValue(1);
        titleFade.setDelay(Duration.millis(600));
        titleFade.play();

        // Sous-titre avec délai
        subtitleLabel.setOpacity(0);
        FadeTransition subFade = new FadeTransition(Duration.millis(800), subtitleLabel);
        subFade.setFromValue(0);
        subFade.setToValue(1);
        subFade.setDelay(Duration.millis(1000));
        subFade.play();

        // Description avec délai
        descriptionLabel.setOpacity(0);
        FadeTransition descFade = new FadeTransition(Duration.millis(800), descriptionLabel);
        descFade.setFromValue(0);
        descFade.setToValue(1);
        descFade.setDelay(Duration.millis(1400));
        descFade.play();

        // Bouton start avec délai
        startLabel.setOpacity(0);
        FadeTransition startFade = new FadeTransition(Duration.millis(800), startLabel);
        startFade.setFromValue(0);
        startFade.setToValue(1);
        startFade.setDelay(Duration.millis(1800));
        startFade.play();
    }

    // Animation des emojis chat/chien
    private void animateEmojis() {
        animateBounce(catEmoji, 0);
        animateBounce(dogEmoji, 200);
    }

    private void animateBounce(Label emoji, int delayMs) {
        emoji.setOpacity(0);

        FadeTransition fade = new FadeTransition(Duration.millis(500), emoji);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(800 + delayMs));
        fade.play();

        // Bounce infini
        TranslateTransition bounce = new TranslateTransition(Duration.millis(1500), emoji);
        bounce.setFromY(0);
        bounce.setToY(-15);
        bounce.setAutoReverse(true);
        bounce.setCycleCount(Animation.INDEFINITE);
        bounce.setInterpolator(Interpolator.EASE_BOTH);
        bounce.setDelay(Duration.millis(1500 + delayMs));
        bounce.play();
    }

    // Animation des cercles décoratifs (flottement)
    private void animateFloatingCircles() {
        animateCircle(circle1, 0, 3000);
        animateCircle(circle2, 1000, 4000);
        animateCircle(circle3, 500, 3500);
        animateCircle(circle4, 1500, 4500);
    }

    private void animateCircle(Circle circle, int delayMs, int durationMs) {
        // Rotation + opacité
        FadeTransition fade = new FadeTransition(Duration.millis(2000), circle);
        fade.setFromValue(0);
        fade.setToValue(0.3);
        fade.setDelay(Duration.millis(delayMs));
        fade.play();

        // Flottement vertical
        TranslateTransition float1 = new TranslateTransition(Duration.millis(durationMs), circle);
        float1.setFromY(0);
        float1.setToY(-30);
        float1.setAutoReverse(true);
        float1.setCycleCount(Animation.INDEFINITE);
        float1.setInterpolator(Interpolator.EASE_BOTH);
        float1.play();

        // Rotation
        RotateTransition rotate = new RotateTransition(Duration.millis(durationMs * 2L), circle);
        rotate.setFromAngle(0);
        rotate.setToAngle(360);
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.play();
    }

    // Animation pulsation du bouton "Démarrer"
    private void animatePulseStart() {
        ScaleTransition pulse = new ScaleTransition(Duration.millis(900), startLabel);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.08);
        pulse.setToY(1.08);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setInterpolator(Interpolator.EASE_BOTH);
        pulse.setDelay(Duration.millis(2500));
        pulse.play();
    }

    // Clic sur "Démarrer" -> transition vers l'analyse
    @FXML
    private void handleStart() {
        // Animation de sortie avant de changer de page
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), mainContainer);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(500), mainContainer);
        scaleOut.setFromX(1);
        scaleOut.setFromY(1);
        scaleOut.setToX(0.95);
        scaleOut.setToY(0.95);

        ParallelTransition exit = new ParallelTransition(fadeOut, scaleOut);
        exit.setOnFinished(e -> MainApp.showAnalysePage());
        exit.play();
    }

    // Effets hover sur le bouton
    @FXML
    private void handleStartHover() {
        startLabel.setStyle(
                "-fx-background-color: white; " +
                        "-fx-text-fill: #667eea; " +
                        "-fx-font-size: 18px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 15 40; " +
                        "-fx-background-radius: 30; " +
                        "-fx-cursor: hand;"
        );
    }

    @FXML
    private void handleStartExit() {
        startLabel.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 18px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 15 40; " +
                        "-fx-background-radius: 30; " +
                        "-fx-border-color: white; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 30; " +
                        "-fx-cursor: hand;"
        );
    }
}