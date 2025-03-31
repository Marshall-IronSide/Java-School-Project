package Election.src;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import java.sql.*;

public class ElectionGUI extends Application {
    private VBox mainLayout;
    private ObservableList<Candidat> candidats = FXCollections.observableArrayList();
    private int totalVotants = 0;

    // AJOUT: Variables pour stocker les résultats
    private List<Candidat> resultatsPremierTour;
    private List<Candidat> resultatsSecondTour;

    // Composants UI
    private TextField tfNomCandidat;
    private ListView<Candidat> listCandidats;
    private TextField tfVotants;
    private Button btnValiderVotants;
    private VBox panelPremierTour;
    private VBox panelSecondTour;
    private GridPane gridPremierTour;
    private GridPane gridSecondTour;
    private Button btnEnregistrerPremierTour;
    private Button btnCalculerPremierTour;
    private Button btnEnregistrerSecondTour;
    private Button btnCalculerSecondTour;

    @Override
    public void start(Stage primaryStage) {
        DatabaseManager.initializeDatabase();
        chargerCandidatsDepuisBDD();

        mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.getStyleClass().add("main-pane");

        creerSectionSaisie();
        creerPanelPremierTour();
        creerPanelSecondTour();

        Scene scene = new Scene(mainLayout, 800, 600);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        primaryStage.setTitle("Gestion des Élections Présidentielles");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void creerSectionSaisie() {
        HBox saisieBox = new HBox(10);
        tfNomCandidat = new TextField();
        tfNomCandidat.setPromptText("Nom du candidat");
        Button btnAjouter = new Button("Ajouter");
        btnAjouter.setOnAction(e -> ajouterCandidat());

        listCandidats = new ListView<>(candidats);
        listCandidats.getStyleClass().add("candidat-list");
        listCandidats.setCellFactory(lv -> new ListCell<Candidat>() {
            @Override
            protected void updateItem(Candidat item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? "" : item.getNom());
            }
        });

        HBox votantsBox = new HBox(10);
        tfVotants = new TextField();
        tfVotants.setPromptText("Nombre total de votants");
        btnValiderVotants = new Button("Valider");
        btnValiderVotants.setOnAction(e -> validerVotants());

        mainLayout.getChildren().addAll(
                new Label("Ajout des candidats:"),
                saisieBox,
                listCandidats,
                new Label("Déclaration des votants:"),
                votantsBox
        );

        saisieBox.getChildren().addAll(tfNomCandidat, btnAjouter);
        votantsBox.getChildren().addAll(tfVotants, btnValiderVotants);
    }

    private void creerPanelPremierTour() {
        panelPremierTour = new VBox(10);
        panelPremierTour.getStyleClass().add("panel");

        gridPremierTour = creerGrilleSuffrages(false);

        btnEnregistrerPremierTour = new Button("Enregistrer les voix");
        btnEnregistrerPremierTour.setOnAction(e -> enregistrerVoixPremierTour());

        btnCalculerPremierTour = new Button("Calculer résultats premier tour");
        btnCalculerPremierTour.setDisable(true);
        btnCalculerPremierTour.setOnAction(e -> calculerPremierTour());

        btnEnregistrerPremierTour.disableProperty().bind(
                Bindings.createBooleanBinding(() ->
                                !tousChampsRemplis(gridPremierTour),
                        gridPremierTour.getChildren()
                )
        );

        panelPremierTour.getChildren().addAll(
                new Label("Premier Tour - Saisie des suffrages:"),
                gridPremierTour,
                new HBox(10, btnEnregistrerPremierTour, btnCalculerPremierTour)
        );
        mainLayout.getChildren().add(panelPremierTour);
        panelPremierTour.setVisible(false);
    }

    private void creerPanelSecondTour() {
        panelSecondTour = new VBox(10);
        panelSecondTour.getStyleClass().add("panel");

        gridSecondTour = creerGrilleSuffrages(true);

        btnEnregistrerSecondTour = new Button("Enregistrer les voix");
        btnEnregistrerSecondTour.setOnAction(e -> enregistrerVoixSecondTour());

        btnCalculerSecondTour = new Button("Calculer résultats second tour");
        btnCalculerSecondTour.setDisable(true);
        btnCalculerSecondTour.setOnAction(e -> calculerSecondTour());

        btnEnregistrerSecondTour.disableProperty().bind(
                Bindings.createBooleanBinding(() ->
                                !tousChampsRemplis(gridSecondTour),
                        gridSecondTour.getChildren()
                )
        );

        panelSecondTour.getChildren().addAll(
                new Label("Second Tour - Saisie des suffrages:"),
                gridSecondTour,
                new HBox(10, btnEnregistrerSecondTour, btnCalculerSecondTour)
        );
        mainLayout.getChildren().add(panelSecondTour);
        panelSecondTour.setVisible(false);
    }

    private GridPane creerGrilleSuffrages(boolean secondTour) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        grid.getChildren().clear();

        List<Candidat> candidates = secondTour ?
                candidats.stream().filter(Candidat::isQualifieSecondTour).collect(Collectors.toList()) :
                candidats;

        for(int i = 0; i < candidates.size(); i++) {
            Candidat c = candidates.get(i);
            TextField tfVotes = new TextField("0");
            grid.addRow(i, new Label(c.getNom()), tfVotes);
        }
        return grid;
    }

    private void enregistrerVoixPremierTour() {
        try {
            List<TextField> champsVotes = gridPremierTour.getChildren().stream()
                    .filter(node -> node instanceof TextField)
                    .map(node -> (TextField) node)
                    .collect(Collectors.toList());

            if(champsVotes.size() != candidats.size()) {
                afficherAlerte("Incohérence détectée! Actualisez la page.", Alert.AlertType.ERROR);
                return;
            }

            for(int i = 0; i < champsVotes.size(); i++) {
                TextField tf = champsVotes.get(i);
                String input = tf.getText().trim();

                if(input.isEmpty()) {
                    afficherAlerte("Champ vide pour " + candidats.get(i).getNom(), Alert.AlertType.ERROR);
                    return;
                }

                try {
                    int votes = Integer.parseInt(input);
                    if(votes < 0) throw new NumberFormatException();
                    candidats.get(i).setSuffragesPremierTour(votes);
                } catch(NumberFormatException e) {
                    afficherAlerte("Valeur invalide pour " + candidats.get(i).getNom(), Alert.AlertType.ERROR);
                    return;
                }
            }

            int totalVotes = candidats.stream().mapToInt(Candidat::getSuffragesPremierTour).sum();
            if(totalVotes > totalVotants) {
                afficherAlerte("Total des votes (" + totalVotes + ") > Votants (" + totalVotants + ")", Alert.AlertType.WARNING);
                return;
            }

            btnCalculerPremierTour.setDisable(false);
            afficherAlerte("Enregistrement réussi!", Alert.AlertType.INFORMATION);

        } catch(Exception e) {
            afficherAlerte("Erreur critique: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // MODIFICATION: Stockage des résultats du premier tour
    private void calculerPremierTour() {
        try {
            if(candidats.isEmpty() || totalVotants == 0) {
                afficherAlerte("Données manquantes pour le calcul!", Alert.AlertType.ERROR);
                return;
            }

            resultatsPremierTour = candidats.stream()
                    .sorted(Comparator.comparingInt(Candidat::getSuffragesPremierTour).reversed())
                    .collect(Collectors.toList());

            afficherDetailsPremierTour(resultatsPremierTour);

            double pourcentage = resultatsPremierTour.get(0).calculerPourcentagePremierTour(totalVotants);

            if(pourcentage > 50) {
                afficherResultatFinal("Vainqueur au premier tour: " + resultatsPremierTour.get(0).getNom());
            } else {
                candidats.forEach(c -> c.setQualifieSecondTour(false));
                resultatsPremierTour.get(0).setQualifieSecondTour(true);
                resultatsPremierTour.get(1).setQualifieSecondTour(true);

                preparerSecondTour();
                panelSecondTour.setVisible(true);
                afficherResultat("Second tour nécessaire! Qualifiés: " +
                        resultatsPremierTour.get(0).getNom() + " et " + resultatsPremierTour.get(1).getNom());
            }

        } catch(Exception e) {
            afficherAlerte("Erreur de calcul: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // MODIFICATION COMPLÈTE: Ajout de l'affichage final
    private void calculerSecondTour() {
        try {
            resultatsSecondTour = candidats.stream()
                    .filter(Candidat::isQualifieSecondTour)
                    .sorted(Comparator.comparingInt(Candidat::getSuffragesSecondTour).reversed())
                    .collect(Collectors.toList());

            if(resultatsSecondTour.isEmpty()) {
                afficherAlerte("Aucun candidat qualifié!", Alert.AlertType.ERROR);
                return;
            }

            afficherResultatsComplets();

            Candidat vainqueur = resultatsSecondTour.get(0);
            afficherResultatFinal("VAINQUEUR FINAL: " + vainqueur.getNom().toUpperCase() +
                    " (" + vainqueur.calculerPourcentageSecondTour(totalVotants) + "%)");

        } catch(Exception e) {
            afficherAlerte("Erreur de calcul: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // NOUVEAU: Affichage consolidé des résultats
    private void afficherResultatsComplets() {
        StringBuilder sb = new StringBuilder();

        sb.append("=== RÉSULTATS DÉTAILLÉS ===\n\n");
        sb.append("--- PREMIER TOUR ---\n");
        for(int i = 0; i < resultatsPremierTour.size(); i++) {
            Candidat c = resultatsPremierTour.get(i);
            sb.append(String.format("%d. %-15s %5d votes   %5.1f%%\n",
                    i+1,
                    c.getNom(),
                    c.getSuffragesPremierTour(),
                    c.calculerPourcentagePremierTour(totalVotants)));
        }

        sb.append("\n--- SECOND TOUR ---\n");
        for(int i = 0; i < resultatsSecondTour.size(); i++) {
            Candidat c = resultatsSecondTour.get(i);
            sb.append(String.format("%d. %-15s %5d votes   %5.1f%%\n",
                    i+1,
                    c.getNom(),
                    c.getSuffragesSecondTour(),
                    c.calculerPourcentageSecondTour(totalVotants)));
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Résultats complets");
        alert.setHeaderText("Détails des deux tours");
        alert.setContentText(sb.toString());
        alert.showAndWait();
    }

    private void validerVotants() {
        try {
            totalVotants = Integer.parseInt(tfVotants.getText());
            if(totalVotants <= 0) throw new NumberFormatException();

            gridPremierTour = creerGrilleSuffrages(false);
            panelPremierTour.getChildren().set(1, gridPremierTour);
            panelPremierTour.setVisible(true);
            btnValiderVotants.setDisable(true);

        } catch(NumberFormatException e) {
            afficherAlerte("Nombre de votants invalide! Doit être > 0", Alert.AlertType.ERROR);
        }
    }

    private void afficherAlerte(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Erreur" : "Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void enregistrerVoixSecondTour() {
        try {
            List<Candidat> qualifiés = candidats.stream()
                    .filter(Candidat::isQualifieSecondTour)
                    .collect(Collectors.toList());

            List<TextField> champsVotes = gridSecondTour.getChildren().stream()
                    .filter(node -> node instanceof TextField)
                    .map(node -> (TextField) node)
                    .collect(Collectors.toList());

            for(int i = 0; i < champsVotes.size(); i++) {
                TextField tf = champsVotes.get(i);
                String input = tf.getText().trim();

                try {
                    int votes = Integer.parseInt(input);
                    if(votes < 0) throw new NumberFormatException();
                    qualifiés.get(i).setSuffragesSecondTour(votes);
                } catch(NumberFormatException e) {
                    afficherAlerte("Valeur invalide pour " + qualifiés.get(i).getNom(), Alert.AlertType.ERROR);
                    return;
                }
            }

            btnCalculerSecondTour.setDisable(false);
            afficherAlerte("Voix enregistrées avec succès!", Alert.AlertType.INFORMATION);

        } catch(Exception e) {
            afficherAlerte("Erreur d'enregistrement: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean tousChampsRemplis(GridPane grid) {
        return grid.getChildren().stream()
                .filter(n -> n instanceof TextField)
                .allMatch(n -> !((TextField)n).getText().isEmpty());
    }

    private void afficherDetailsPremierTour(List<Candidat> classement) {
        StringBuilder sb = new StringBuilder("Classement premier tour:\n");
        for(int i = 0; i < classement.size(); i++) {
            Candidat c = classement.get(i);
            sb.append(String.format("%d. %-15s %5d votes   %5.1f%%\n",
                    i+1,
                    c.getNom(),
                    c.getSuffragesPremierTour(),
                    c.calculerPourcentagePremierTour(totalVotants)));
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Détails du premier tour");
        alert.setContentText(sb.toString());
        alert.showAndWait();
    }

    // MODIFICATION: Renommé pour plus de clarté
    private void afficherResultatFinal(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Résultat final");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void preparerSecondTour() {
        List<Candidat> qualifiés = candidats.stream()
                .filter(Candidat::isQualifieSecondTour)
                .collect(Collectors.toList());

        gridSecondTour = creerGrilleSuffrages(true);
        panelSecondTour.getChildren().set(1, gridSecondTour);
    }

    private void chargerCandidatsDepuisBDD() {
        candidats.clear();
        String sql = "SELECT * FROM candidats";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Candidat c = new Candidat(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getInt("suffrages_premier"),
                        rs.getInt("suffrages_second"),
                        rs.getBoolean("qualifie")
                );
                candidats.add(c);
            }
        } catch (SQLException e) {
            afficherAlerte("Erreur de chargement BDD: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void ajouterCandidat() {
        String nom = tfNomCandidat.getText().trim();
        if(!nom.isEmpty() && candidats.stream().noneMatch(c -> c.getNom().equalsIgnoreCase(nom))) {
            try {
                Candidat nouveau = new Candidat(nom);
                DatabaseManager.sauvegarderCandidat(nouveau);
                candidats.add(nouveau);
                tfNomCandidat.clear();
            } catch(Exception e) {
                afficherAlerte("Erreur BDD: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            afficherAlerte("Nom invalide ou déjà existant!", Alert.AlertType.ERROR);
        }
    }

    private void afficherResultat(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Résultat intermédiaire");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}