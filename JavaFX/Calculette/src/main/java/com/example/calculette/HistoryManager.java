package com.example.calculette;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.List;

public class HistoryManager {

    private Stage historyStage;
    private ListView<String> historyView;

    public void showHistory(List<String> history) {
        if (historyStage == null) {
            historyStage = new Stage();
            historyStage.setTitle("Historique des Calculs");

            VBox historyLayout = new VBox(10);
            historyLayout.setPadding(new Insets(20));
            historyLayout.setStyle("-fx-background-color: #1c1c1c;");

            Label historyLabel = new Label("Historique des Calculs");
            historyLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

            historyView = new ListView<>();
            historyView.setPrefHeight(400);
            historyView.setPrefWidth(300);
            historyView.setStyle("-fx-background-color: #1c1c1c; -fx-text-fill: white; -fx-font-size: 14px;");
            historyView.setItems(FXCollections.observableArrayList(history));

            Button clearHistoryButton = new Button("Effacer l'Historique");
            clearHistoryButton.setStyle("-fx-background-color: #FF3B30; -fx-text-fill: white; -fx-font-size: 14px; " +
                    "-fx-padding: 8px; -fx-background-radius: 5px;");
            clearHistoryButton.setOnAction(e -> {
                history.clear();
                historyView.setItems(FXCollections.observableArrayList(history));
            });

            historyLayout.getChildren().addAll(historyLabel, historyView, clearHistoryButton);

            Scene scene = new Scene(historyLayout, 340, 500);
            historyStage.setScene(scene);
            historyStage.setResizable(false);
        }

        // Mise à jour de la liste avant d'afficher la fenêtre
        historyView.setItems(FXCollections.observableArrayList(history));
        historyStage.show();
    }

    public boolean isHistoryStageShowing() {
        return historyStage != null && historyStage.isShowing();
    }

    public void updateHistoryView(List<String> history) {
        if (historyView != null && historyStage != null && historyStage.isShowing()) {
            historyView.setItems(FXCollections.observableArrayList(history));
        }
    }

    public void updateHistoryView() {
        if (historyView != null && historyStage != null && historyStage.isShowing()) {
            historyView.refresh();
        }
    }
}