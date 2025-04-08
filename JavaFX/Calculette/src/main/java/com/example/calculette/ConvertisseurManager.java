package com.example.calculette;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.List;

public class ConvertisseurManager {

    // Taux de change par défaut
    private double eurToXofRate = 655.96;
    private double xofToEurRate = 0.001524;

    private List<String> history;
    private HistoryManager historyManager;

    public ConvertisseurManager(List<String> history, HistoryManager historyManager) {
        this.history = history;
        this.historyManager = historyManager;
    }

    public void openConversionWindow() {
        Stage conversionStage = new Stage();
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f0f0f0;");
        layout.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Convertisseur de Devises");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        Label subtitleLabel = new Label("XOF = Franc CFA (Communauté Financière Africaine)");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");

        // Affichage du taux actuel
        Label rateLabel = new Label(String.format("Taux actuel : 1 EUR = %.2f XOF", eurToXofRate).replace(".", ","));
        rateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #007AFF; -fx-font-weight: bold;");

        // Bouton pour modifier les taux de change
        Button changeRateButton = new Button("Modifier les taux");
        changeRateButton.setStyle("-fx-background-color: #007AFF; -fx-text-fill: white; -fx-font-size: 14px; " +
                "-fx-padding: 8px; -fx-background-radius: 10px;");
        changeRateButton.setOnAction(e -> openRateChangeWindow(conversionStage, rateLabel));

        TextField amountField = new TextField();
        amountField.setPromptText("Entrez le montant");
        amountField.setStyle("-fx-font-size: 16px; -fx-padding: 10px; -fx-background-radius: 10px;");
        amountField.setMaxWidth(300);

        ComboBox<String> fromCurrency = new ComboBox<>();
        fromCurrency.getItems().addAll("EUR", "XOF");
        fromCurrency.setPromptText("Devise d'origine");
        fromCurrency.setStyle("-fx-font-size: 16px; -fx-background-radius: 10px;");
        fromCurrency.setMaxWidth(300);

        ComboBox<String> toCurrency = new ComboBox<>();
        toCurrency.getItems().addAll("EUR", "XOF");
        toCurrency.setPromptText("Devise de destination");
        toCurrency.setStyle("-fx-font-size: 16px; -fx-background-radius: 10px;");
        toCurrency.setMaxWidth(300);

        Button convertButton = new Button("Convertir");
        convertButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px; " +
                "-fx-padding: 10px 20px; -fx-background-radius: 10px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 5, 0, 0, 1);");
        convertButton.setMaxWidth(300);

        Label resultLabel = new Label("Résultat : ");
        resultLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        convertButton.setOnAction(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText().replace(",", "."));
                String from = fromCurrency.getValue();
                String to = toCurrency.getValue();

                if (from == null || to == null) {
                    resultLabel.setText("Veuillez sélectionner les devises !");
                    return;
                }

                double result = convertCurrency(amount, from, to);
                String resultText = String.format("%.2f %s = %.2f %s", amount, from, result, to);
                resultLabel.setText("Résultat : " + resultText.replace(".", ","));
                addToHistory(resultText.replace(".", ","));
            } catch (NumberFormatException ex) {
                resultLabel.setText("Entrée invalide !");
            }
        });

        layout.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                rateLabel,
                changeRateButton,
                amountField,
                fromCurrency,
                toCurrency,
                convertButton,
                resultLabel
        );

        Scene scene = new Scene(layout, 400, 500);
        conversionStage.setScene(scene);
        conversionStage.setTitle("Convertisseur de Devises");
        conversionStage.setResizable(false);
        conversionStage.show();
    }

    private void openRateChangeWindow(Stage parentStage, Label rateLabel) {
        Stage rateStage = new Stage();
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f0f0f0;");
        layout.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Modifier les taux de change");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        // Champ pour EUR vers XOF
        Label eurToXofLabel = new Label("1 EUR = ? XOF");
        eurToXofLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #333333;");

        TextField eurToXofField = new TextField(String.format("%.2f", eurToXofRate).replace(".", ","));
        eurToXofField.setStyle("-fx-font-size: 16px; -fx-padding: 10px; -fx-background-radius: 10px;");
        eurToXofField.setMaxWidth(200);

        // Champ pour XOF vers EUR
        Label xofToEurLabel = new Label("1 XOF = ? EUR");
        xofToEurLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #333333;");

        TextField xofToEurField = new TextField(String.format("%.6f", xofToEurRate).replace(".", ","));
        xofToEurField.setStyle("-fx-font-size: 16px; -fx-padding: 10px; -fx-background-radius: 10px;");
        xofToEurField.setMaxWidth(200);

        // Option de synchronisation automatique
        CheckBox syncCheckbox = new CheckBox("Synchroniser automatiquement les taux (1/X)");
        syncCheckbox.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
        syncCheckbox.setSelected(true);

        // Événement pour synchroniser les taux
        eurToXofField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (syncCheckbox.isSelected()) {
                try {
                    double rate = Double.parseDouble(newValue.replace(",", "."));
                    if (rate > 0) {
                        double inverseRate = 1.0 / rate;
                        xofToEurField.setText(String.format("%.6f", inverseRate).replace(".", ","));
                    }
                } catch (NumberFormatException e) {
                    // Ignorer les entrées non numériques
                }
            }
        });

        xofToEurField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (syncCheckbox.isSelected()) {
                try {
                    double rate = Double.parseDouble(newValue.replace(",", "."));
                    if (rate > 0) {
                        double inverseRate = 1.0 / rate;
                        eurToXofField.setText(String.format("%.2f", inverseRate).replace(".", ","));
                    }
                } catch (NumberFormatException e) {
                    // Ignorer les entrées non numériques
                }
            }
        });

        Button saveButton = new Button("Enregistrer");
        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px; " +
                "-fx-padding: 10px 20px; -fx-background-radius: 10px;");
        saveButton.setMaxWidth(200);

        saveButton.setOnAction(e -> {
            try {
                double newEurToXofRate = Double.parseDouble(eurToXofField.getText().replace(",", "."));
                double newXofToEurRate = Double.parseDouble(xofToEurField.getText().replace(",", "."));

                if (newEurToXofRate > 0 && newXofToEurRate > 0) {
                    eurToXofRate = newEurToXofRate;
                    xofToEurRate = newXofToEurRate;

                    // Mettre à jour l'affichage du taux dans la fenêtre parent
                    rateLabel.setText(String.format("Taux actuel : 1 EUR = %.2f XOF", eurToXofRate).replace(".", ","));

                    rateStage.close();
                } else {
                    StyleManager.showAlert("Erreur", "Les taux doivent être supérieurs à zéro.");
                }
            } catch (NumberFormatException ex) {
                StyleManager.showAlert("Erreur", "Veuillez entrer des valeurs numériques valides.");
            }
        });

        Button cancelButton = new Button("Annuler");
        cancelButton.setStyle("-fx-background-color: #999999; -fx-text-fill: white; -fx-font-size: 16px; " +
                "-fx-padding: 10px 20px; -fx-background-radius: 10px;");
        cancelButton.setMaxWidth(200);
        cancelButton.setOnAction(e -> rateStage.close());

        // Bouton pour réinitialiser les taux par défaut
        Button resetButton = new Button("Réinitialiser les taux par défaut");
        resetButton.setStyle("-fx-background-color: #FF9F0A; -fx-text-fill: white; -fx-font-size: 14px; " +
                "-fx-padding: 8px; -fx-background-radius: 10px;");
        resetButton.setMaxWidth(250);
        resetButton.setOnAction(e -> {
            eurToXofField.setText("655,96");
            xofToEurField.setText("0,001524");
        });

        HBox buttonBox = new HBox(10, saveButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(
                titleLabel,
                eurToXofLabel,
                eurToXofField,
                xofToEurLabel,
                xofToEurField,
                syncCheckbox,
                resetButton,
                buttonBox
        );

        Scene scene = new Scene(layout, 350, 400);
        rateStage.setScene(scene);
        rateStage.setTitle("Modifier les taux");
        rateStage.setResizable(false);
        rateStage.show();
    }

    private double convertCurrency(double amount, String from, String to) {
        // Utiliser les taux définis comme variables d'instance
        if (from.equals("EUR") && to.equals("XOF")) {
            return amount * eurToXofRate;
        } else if (from.equals("XOF") && to.equals("EUR")) {
            return amount * xofToEurRate;
        } else {
            return amount; // Même devise
        }
    }

    private void addToHistory(String calculation) {
        history.add(0, calculation);
        if (history.size() > 5) {
            history.remove(history.size() - 1);
        }
        if (historyManager.isHistoryStageShowing()) {
            historyManager.updateHistoryView(history);
        }
    }
}