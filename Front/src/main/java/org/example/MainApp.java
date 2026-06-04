package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static Stage primaryStage;
    private static Scene mainScene;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        try {
            // Chargement de la page d'accueil par défaut
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/frontend/welcome.fxml"));
            Parent root = loader.load();

            mainScene = new Scene(root, 1000, 700);
            mainScene.getStylesheets().add(MainApp.class.getResource("/frontend/style.css").toExternalForm());

            primaryStage.setTitle("Classification Chat/Chien/Wild — ISEN Groupe 9");
            primaryStage.setScene(mainScene);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);

            primaryStage.setMaximized(true);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showWelcomePage() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/frontend/welcome.fxml"));
            Parent root = loader.load();

            if (mainScene != null) {
                mainScene.setRoot(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showAnalysePage() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/frontend/interface.fxml"));
            Parent root = loader.load();

            if (mainScene != null) {
                mainScene.setRoot(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}