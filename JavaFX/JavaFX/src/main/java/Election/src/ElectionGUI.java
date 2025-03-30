package Election.src;

import javafx.application.Application;
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
import javafx.beans.binding.Bindings;
import javafx.scene.control.TextField;

public class ElectionGUI extends Application {
    private VBox mainLayout;
    private ObservableList<Candidat> candidats = FXCollections.observableArrayList();
    private int totalVotants = 0;

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

        // Vider complètement la grille avant reconstruction
        grid.getChildren().clear();

        List<Candidat> candidates = secondTour ?
                candidats.stream().filter(Candidat::isQualifieSecondTour).collect(Collectors.toList()) :
                candidats;

        // Ajouter une ligne par candidat
        for(int i = 0; i < candidates.size(); i++) {
            Candidat c = candidates.get(i);
            grid.addRow(i,
                    new Label(c.getNom()),
                    new TextField("0") // Toujours créer de nouveaux composants
            );
        }
        return grid;
    }

    private void enregistrerVoixPremierTour() {
        try {
            List<Candidat> candidatsTemporaires = new ArrayList<>(candidats);

            // Filtrer uniquement les TextField
            List<TextField> champsVotes = gridPremierTour.getChildren().stream()
                    .filter(node -> node instanceof TextField)
                    .map(node -> (TextField) node)
                    .collect(Collectors.toList());

            // Vérifier la correspondance candidats/champs
            if (champsVotes.size() != candidatsTemporaires.size()) {
                afficherAlerte("Incohérence détectée! Merci de recharger l'interface.", Alert.AlertType.ERROR);
                return;
            }

            // Parcourir les TextField et candidats en parallèle
            for (int i = 0; i < champsVotes.size(); i++) {
                TextField tf = champsVotes.get(i);
                String input = tf.getText().trim();
                Candidat c = candidatsTemporaires.get(i);

                if (input.isEmpty()) {
                    afficherAlerte("Champ vide pour " + c.getNom(), Alert.AlertType.ERROR);
                    return;
                }

                try {
                    int votes = Integer.parseInt(input);
                    if (votes < 0) throw new NumberFormatException();
                    c.setSuffragesPremierTour(votes);
                } catch (NumberFormatException e) {
                    afficherAlerte("Valeur invalide pour " + c.getNom() + "\nDoit être un entier positif", Alert.AlertType.ERROR);
                    return;
                }
            }

            // Mise à jour atomique
            candidats.setAll(candidatsTemporaires);
            btnCalculerPremierTour.setDisable(false);
            afficherAlerte("Voix enregistrées avec succès !", Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            afficherAlerte("Erreur critique: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    // Méthode d'alerte améliorée
    private void afficherAlerte(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Erreur" : "Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void enregistrerVoixSecondTour() {
        int row = 0;
        List<Candidat> qualifiés = candidats.stream()
                .filter(Candidat::isQualifieSecondTour)
                .collect(Collectors.toList());

        for(Node node : gridSecondTour.getChildren()) {
            if(node instanceof TextField) {
                TextField tf = (TextField) node;
                try {
                    int votes = Integer.parseInt(tf.getText());
                    qualifiés.get(row).setSuffragesSecondTour(votes);
                    row++;
                } catch (NumberFormatException e) {
                    afficherAlerte("Valeur invalide pour " + qualifiés.get(row).getNom());
                    return;
                }
            }
        }
        btnCalculerSecondTour.setDisable(false);
        afficherAlerte("Voix enregistrées avec succès !");
    }

    private boolean tousChampsRemplis(GridPane grid) {
        return grid.getChildren().stream()
                .filter(n -> n instanceof TextField)
                .allMatch(n -> !((TextField)n).getText().isEmpty());
    }

    private void calculerPremierTour() {
        // Récupérer les votes depuis l'interface
        List<Candidat> candidatsAvecVotes = new ArrayList<>();
        GridPane grid = (GridPane) panelPremierTour.getChildren().get(1);
        int rowIndex = 0;
        for(Node node : grid.getChildren()) {
            if (node instanceof TextField) {
                TextField tfVotes = (TextField) node;
                try {
                    int votes = Integer.parseInt(tfVotes.getText());
                    candidats.get(rowIndex).setSuffragesPremierTour(votes);
                    rowIndex++;
                } catch (NumberFormatException e) {
                    afficherAlerte("Valeur invalide pour " + candidats.get(rowIndex).getNom());
                    return; // Arrêter le calcul si une valeur est incorrecte
                }
            }
            if (rowIndex != candidats.size()) {
                afficherAlerte("Veuillez saisir tous les suffrages !");
                return;
            }

        }


        // Tri par votes décroissants
        List<Candidat> classement = candidats.stream()
                .sorted(Comparator.comparingInt(Candidat::getSuffragesPremierTour).reversed())
                .collect(Collectors.toList());

        // Calcul pourcentage
        double pourcentagePremier = classement.get(0).calculerPourcentagePremierTour(totalVotants);

        if(pourcentagePremier > 50) {
            afficherResultat("Vainqueur au premier tour : " + classement.get(0).getNom());
        } else {
            // Réinitialiser les qualifications avant de sélectionner
            candidats.forEach(c -> c.setQualifieSecondTour(false));

            // Sélectionner uniquement les 2 premiers
            classement.get(0).setQualifieSecondTour(true);
            classement.get(1).setQualifieSecondTour(true);

            // Afficher le panel du second tour
            preparerSecondTour();
            panelSecondTour.setVisible(true);

            // Afficher les résultats du premier tour AVANT le second
            afficherResultat("Second tour nécessaire! Qualifiés : "
                    + classement.get(0).getNom() + " et " + classement.get(1).getNom());
        }

        // Toujours afficher les résultats détaillés du premier tour
        afficherDetailsPremierTour(classement);
    }
    private void validerVotants() {
        try {
            totalVotants = Integer.parseInt(tfVotants.getText());
            if(totalVotants <= 0) throw new NumberFormatException();

            // Recréer la grille pour synchroniser avec les candidats actuels
            gridPremierTour = creerGrilleSuffrages(false);
            panelPremierTour.getChildren().set(1, gridPremierTour); // Mettre à jour la grille

            panelPremierTour.setVisible(true);
            btnValiderVotants.setDisable(true);

        } catch (NumberFormatException e) {
            afficherAlerte("Nombre de votants invalide ! Doit être > 0");
        }
    }
    private void afficherDetailsPremierTour(List<Candidat> classement) {
        StringBuilder sb = new StringBuilder("Résultats premier tour:\n");
        for(Candidat c : classement) {
            sb.append(String.format("- %s: %d votes (%.1f%%)\n",
                    c.getNom(),
                    c.getSuffragesPremierTour(),
                    c.calculerPourcentagePremierTour(totalVotants)));
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Détails du premier tour");
        alert.setContentText(sb.toString());
        alert.showAndWait();
    }

    private void preparerSecondTour() {
        List<Candidat> qualifiés = candidats.stream()
                .filter(Candidat::isQualifieSecondTour)
                .collect(Collectors.toList());

        GridPane grid = new GridPane();
        qualifiés.forEach(c -> {
            TextField tf = new TextField("0");
            grid.addRow(grid.getRowCount(), new Label(c.getNom()), tf);
        });

        panelSecondTour.getChildren().add(1, grid);
    }

    private void calculerSecondTour() {
        List<Candidat> qualifiés = candidats.stream()
                .filter(Candidat::isQualifieSecondTour)
                .sorted(Comparator.comparingInt(Candidat::getSuffragesSecondTour).reversed())
                .collect(Collectors.toList());

        afficherResultat("Vainqueur au second tour: " + qualifiés.get(0).getNom());
    }

    private void chargerCandidatsDepuisBDD() {
        // Implémenter la logique de chargement depuis MySQL
    }
    private void ajouterCandidat() {
        String nom = tfNomCandidat.getText().trim();
        if (!nom.isEmpty() && candidats.stream().noneMatch(c -> c.getNom().equalsIgnoreCase(nom))) {
            try {
                Candidat nouveau = new Candidat(nom);
                DatabaseManager.sauvegarderCandidat(nouveau);
                candidats.add(nouveau);
                tfNomCandidat.clear();
            } catch (Exception e) {
                afficherAlerte("Erreur d'ajout dans la BDD: " + e.getMessage());
            }
        } else {
            afficherAlerte("Nom invalide ou déjà existant !");
        }
    }

    private void afficherAlerte(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void afficherResultat(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Résultat des élections");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}