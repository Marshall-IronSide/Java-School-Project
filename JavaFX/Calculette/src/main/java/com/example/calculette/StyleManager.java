package com.example.calculette;

import javafx.scene.control.Alert;

public class StyleManager {

    public static String getButtonStyle(String text) {
        String baseStyle = "-fx-background-radius: 35; -fx-border-radius: 35; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 5, 0, 0, 1);";
        switch (text) {
            case "AC":
            case "⌫":
            case "%":
                return baseStyle + "-fx-background-color: #A5A5A5; -fx-text-fill: black;";
            case "÷":
            case "×":
            case "-":
            case "+":
            case "=":
                return baseStyle + "-fx-background-color: #FF9F0A; -fx-text-fill: white;";
            case "Conv":
                return baseStyle + "-fx-background-color: #4CAF50; -fx-text-fill: white;";
            default:
                return baseStyle + "-fx-background-color: #333333; -fx-text-fill: white;";
        }
    }

    public static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}