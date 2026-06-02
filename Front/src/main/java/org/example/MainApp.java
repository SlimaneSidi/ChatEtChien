package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        try {
            showWelcomePage();
            primaryStage.setTitle("Classification Chat/Chien");
            primaryStage.setResizable(true);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Affiche la page d'accueil
    public static void showWelcomePage() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource("/frontend/welcome.fxml")
            );
            BorderPane root = loader.load();
            Scene scene = new Scene(root, 900, 700);
            scene.getStylesheets().add(
                    MainApp.class.getResource("/frontend/style.css").toExternalForm()
            );
            primaryStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Affiche la page d'analyse
    public static void showAnalysePage() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource("/frontend/interface.fxml")
            );
            BorderPane root = loader.load();
            Scene scene = new Scene(root, 900, 700);
            scene.getStylesheets().add(
                    MainApp.class.getResource("/frontend/style.css").toExternalForm()
            );
            primaryStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}