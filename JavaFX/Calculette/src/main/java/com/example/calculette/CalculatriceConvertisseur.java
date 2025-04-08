package com.example.calculette;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;

public class CalculatriceConvertisseur extends Application {
    private Label display;
    private Label operationDisplay;
    private String currentInput = "";
    private String operator = "";
    private double firstOperand = 0;
    private List<String> history = new ArrayList<>();
    private HistoryManager historyManager;
    private ConvertisseurManager convertisseurManager;

    @Override
    public void start(Stage primaryStage) {
        // Initialiser les gestionnaires
        historyManager = new HistoryManager();
        convertisseurManager = new ConvertisseurManager(history, historyManager);

        // Créer l'interface principale
        VBox displayBox = createDisplayBox();
        GridPane grid = createButtonGrid();
        Button historyButton = createHistoryButton();

        VBox root = new VBox(0, historyButton, displayBox, grid);
        root.setStyle("-fx-background-color: black;");
        Scene scene = new Scene(root, 320, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Calculatrice iPhone");
        primaryStage.show();
    }

    private VBox createDisplayBox() {
        VBox displayBox = new VBox(5);
        displayBox.setStyle("-fx-background-color: black;");

        operationDisplay = new Label("");
        operationDisplay.setStyle("-fx-text-fill: #666666; -fx-font-size: 24px;");
        operationDisplay.setAlignment(Pos.CENTER_RIGHT);
        operationDisplay.setPrefWidth(300);

        display = new Label("0");
        display.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-padding: 20px;");
        display.setFont(Font.font("System", FontWeight.LIGHT, 48));
        display.setAlignment(Pos.CENTER_RIGHT);
        display.setPrefWidth(300);
        display.setMinHeight(100);

        displayBox.getChildren().addAll(operationDisplay, display);
        return displayBox;
    }

    private GridPane createButtonGrid() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-background-color: black;");

        String[][] buttons = {
                {"AC", "⌫", "%", "÷"},
                {"7", "8", "9", "×"},
                {"4", "5", "6", "-"},
                {"1", "2", "3", "+"},
                {"0", ".", "=", "Conv"}
        };

        for (int i = 0; i < buttons.length; i++) {
            for (int j = 0; j < buttons[i].length; j++) {
                Button button = new Button(buttons[i][j]);
                button.setFont(Font.font("System", FontWeight.LIGHT, 24));
                button.setPrefSize(70, 70);
                button.setStyle(StyleManager.getButtonStyle(buttons[i][j]));

                // Add click animation
                addButtonAnimation(button);

                button.setOnAction(e -> handleButtonPress(button.getText()));
                grid.add(button, j, i);
            }
        }

        // Make the zero button span two columns
        Button zeroButton = (Button) grid.getChildren().stream()
                .filter(node -> node instanceof Button && ((Button) node).getText().equals("0"))
                .findFirst()
                .orElse(null);
        if (zeroButton != null) {
            GridPane.setColumnSpan(zeroButton, 2);
            zeroButton.setPrefWidth(150);
        }

        return grid;
    }

    private void addButtonAnimation(Button button) {
        button.setOnMousePressed(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
            st.setToX(0.95);
            st.setToY(0.95);
            st.playFromStart();
        });

        button.setOnMouseReleased(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
            st.setToX(1.0);
            st.setToY(1.0);
            st.playFromStart();
        });
    }

    private Button createHistoryButton() {
        Button historyButton = new Button("Historique");
        historyButton.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-font-size: 16px; " +
                "-fx-background-radius: 5px; -fx-padding: 5px 10px;");
        historyButton.setOnAction(e -> historyManager.showHistory(history));
        return historyButton;
    }

    private void updateOperationDisplay() {
        if (!operator.isEmpty()) {
            // Utilisez le formatage conditionnel pour afficher soit un entier, soit un nombre à virgule
            String firstOperandStr = firstOperand == Math.floor(firstOperand) ?
                    String.valueOf((int)firstOperand) :
                    String.valueOf(firstOperand);
            operationDisplay.setText(String.format("%s %s %s", firstOperandStr, operator, currentInput));
        } else {
            operationDisplay.setText(currentInput);
        }
    }

    private void handleButtonPress(String text) {
        switch (text) {
            case "AC":
                currentInput = "";
                firstOperand = 0;
                operator = "";
                display.setText("0");
                operationDisplay.setText("");
                break;
            case "⌫":
                if (!currentInput.isEmpty()) {
                    currentInput = currentInput.substring(0, currentInput.length() - 1);
                    display.setText(currentInput.isEmpty() ? "0" : currentInput);
                    updateOperationDisplay();
                }
                break;
            case "%":
                if (!currentInput.isEmpty()) {
                    double value = Double.parseDouble(currentInput);
                    currentInput = String.valueOf(value / 100);
                    display.setText(currentInput);
                    updateOperationDisplay();
                }
                break;
            case "÷":
            case "×":
            case "-":
            case "+":
                if (!currentInput.isEmpty()) {
                    if (!operator.isEmpty()) {
                        computeResult();
                    }
                    firstOperand = Double.parseDouble(currentInput);
                    operator = text;
                    currentInput = "";
                    updateOperationDisplay();
                }
                break;
            case "=":
                computeResult();
                break;
            case "Conv":
                convertisseurManager.openConversionWindow();
                break;
            case ".":
                // Ajouter le point décimal s'il n'existe pas déjà dans l'entrée
                if (!currentInput.contains(".")) {
                    if (currentInput.isEmpty()) {
                        currentInput = "0.";
                    } else {
                        currentInput += ".";
                    }
                    display.setText(currentInput);
                    updateOperationDisplay();
                }
                break;
            default:
                if (currentInput.equals("0")) {
                    currentInput = text;
                } else {
                    currentInput += text;
                }
                display.setText(currentInput);
                updateOperationDisplay();
        }
    }

    private void computeResult() {
        if (!currentInput.isEmpty() && !operator.isEmpty()) {
            double secondOperand = Double.parseDouble(currentInput);
            double result = 0;
            switch (operator) {
                case "+":
                    result = firstOperand + secondOperand;
                    break;
                case "-":
                    result = firstOperand - secondOperand;
                    break;
                case "×":
                    result = firstOperand * secondOperand;
                    break;
                case "÷":
                    if (secondOperand == 0) {
                        display.setText("Error");
                        operationDisplay.setText("");
                        currentInput = "";
                        operator = "";
                        return;
                    }
                    result = firstOperand / secondOperand;
                    break;
            }

            // Formatage pour l'affichage : entier si le résultat est un nombre entier
            String firstOperandStr = firstOperand == Math.floor(firstOperand) ?
                    String.valueOf((int)firstOperand) :
                    String.valueOf(firstOperand);
            String secondOperandStr = secondOperand == Math.floor(secondOperand) ?
                    String.valueOf((int)secondOperand) :
                    String.valueOf(secondOperand);
            String resultStr = result == Math.floor(result) ?
                    String.valueOf((int)result) :
                    String.valueOf(result);

            String resultText = String.format("%s %s %s = %s", firstOperandStr, operator, secondOperandStr, resultStr);
            display.setText(resultStr);
            addToHistory(resultText);

            // Mettre à jour pour permettre les calculs en chaîne
            firstOperand = result;
            currentInput = String.valueOf(result);
            operator = "";
            operationDisplay.setText("");
        }
    }

    private void addToHistory(String calculation) {
        history.add(0, calculation);
        if (history.size() > 5) {
            history.remove(history.size() - 1);
        }

        // Appel direct à showHistory si l'historique est déjà ouvert
        if (historyManager.isHistoryStageShowing()) {
            historyManager.updateHistoryView(history);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}