package com.example.calculette;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

public class CalculatriceController implements Initializable {

    // Taux de conversion par défaut (1 Euro = 655.957 Francs CFA)
    private double tauxConversion = 655.957;
    private DecimalFormat df = new DecimalFormat("#.##");

    // Variables pour les calculs
    private double nombre1 = 0;
    private String operateur = "";
    private boolean debutSaisie = true;

    @FXML
    private TabPane tabPane;

    @FXML
    private Tab tabCalculatrice;

    @FXML
    private Tab tabConvertisseur;

    // Éléments de l'interface calculatrice
    @FXML
    private TextField ecranCalculatrice;

    // Éléments de l'interface convertisseur
    @FXML
    private TextField tauxTextField;

    @FXML
    private TextField montantFCFA;

    @FXML
    private TextField montantEuro;

    @FXML
    private Label messageTaux;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialisation de l'interface
        ecranCalculatrice.setText("0");
        tauxTextField.setText(String.valueOf(tauxConversion));

        // Empêcher la saisie directe dans l'écran de la calculatrice
        ecranCalculatrice.setEditable(false);
    }

    // Méthodes pour la calculatrice
    @FXML
    void handleDigit(MouseEvent event) {
        String chiffre = ((Button) event.getSource()).getText();

        if (debutSaisie) {
            ecranCalculatrice.setText(chiffre);
            debutSaisie = false;
        } else {
            if (ecranCalculatrice.getText().equals("0")) {
                ecranCalculatrice.setText(chiffre);
            } else {
                ecranCalculatrice.setText(ecranCalculatrice.getText() + chiffre);
            }
        }
    }

    @FXML
    void handleDecimal(MouseEvent event) {
        if (debutSaisie) {
            ecranCalculatrice.setText("0.");
            debutSaisie = false;
        } else if (!ecranCalculatrice.getText().contains(".")) {
            ecranCalculatrice.setText(ecranCalculatrice.getText() + ".");
        }
    }

    @FXML
    void handleOperator(MouseEvent event) {
        String nouvelOperateur = ((Button) event.getSource()).getText();

        if (!operateur.isEmpty()) {
            calculer();
        }

        nombre1 = Double.parseDouble(ecranCalculatrice.getText());
        operateur = nouvelOperateur;
        debutSaisie = true;
    }

    @FXML
    void handleEquals(MouseEvent event) {
        if (!operateur.isEmpty()) {
            calculer();
            operateur = "";
        }
    }

    @FXML
    void handleClear(MouseEvent event) {
        ecranCalculatrice.setText("0");
        operateur = "";
        nombre1 = 0;
        debutSaisie = true;
    }

    private void calculer() {
        double nombre2 = Double.parseDouble(ecranCalculatrice.getText());
        double resultat = 0;

        switch (operateur) {
            case "+":
                resultat = nombre1 + nombre2;
                break;
            case "-":
                resultat = nombre1 - nombre2;
                break;
            case "×":
                resultat = nombre1 * nombre2;
                break;
            case "÷":
                if (nombre2 != 0) {
                    resultat = nombre1 / nombre2;
                } else {
                    ecranCalculatrice.setText("Erreur");
                    debutSaisie = true;
                    return;
                }
                break;
        }

        // Formatage du résultat pour éviter les nombres trop longs
        ecranCalculatrice.setText(df.format(resultat).replace(',', '.'));
        debutSaisie = true;
    }

    // Méthodes pour le convertisseur
    @FXML
    void convertirFCFAversEuro() {
        try {
            double montant = Double.parseDouble(montantFCFA.getText().replace(',', '.'));
            double resultat = montant / tauxConversion;
            montantEuro.setText(df.format(resultat));
        } catch (NumberFormatException e) {
            montantEuro.setText("Erreur");
        }
    }

    @FXML
    void convertirEuroVersFCFA() {
        try {
            double montant = Double.parseDouble(montantEuro.getText().replace(',', '.'));
            double resultat = montant * tauxConversion;
            montantFCFA.setText(df.format(resultat));
        } catch (NumberFormatException e) {
            montantFCFA.setText("Erreur");
        }
    }

    @FXML
    void modifierTaux() {
        try {
            double nouveauTaux = Double.parseDouble(tauxTextField.getText().replace(',', '.'));
            if (nouveauTaux > 0) {
                tauxConversion = nouveauTaux;
                messageTaux.setText("Taux mis à jour avec succès");
                messageTaux.setStyle("-fx-text-fill: green;");
            } else {
                messageTaux.setText("Le taux doit être positif");
                messageTaux.setStyle("-fx-text-fill: red;");
            }
        } catch (NumberFormatException e) {
            messageTaux.setText("Format invalide");
            messageTaux.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    void reinitialiserConvertisseur() {
        montantFCFA.clear();
        montantEuro.clear();
        messageTaux.setText("");
    }
}