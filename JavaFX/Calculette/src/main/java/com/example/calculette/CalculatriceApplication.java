package com.example.calculette;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CalculatriceApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Chargement du fichier FXML depuis le bon chemin
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/calculette/calculatrice.fxml"));
        Parent root = loader.load();

        // Configuration de la scène
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/example/calculette/style.css").toExternalForm());

        // Configuration de la fenêtre principale
        primaryStage.setTitle("Calculatrice & Convertisseur Franc CFA/Euro");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}