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
        Button btnCalculer = new Button("Calculer résultats premier tour");
        btnCalculer.setOnAction(e -> calculerPremierTour());

        panelPremierTour.getChildren().addAll(
                new Label("Premier Tour - Saisie des suffrages:"),
                creerGrilleSuffrages(),
                btnCalculer
        );
        mainLayout.getChildren().add(panelPremierTour);
        panelPremierTour.setVisible(false);
    }

    private GridPane creerGrilleSuffrages() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        int row = 0;

        for(Candidat c : candidats) {
            TextField tfVotes = new TextField("0");
            tfVotes.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches("\\d*")) tfVotes.setText(oldVal);
            });

            grid.addRow(row++,
                    new Label(c.getNom()),
                    tfVotes
            );
        }
        return grid;
    }

    private void creerPanelSecondTour() {
        panelSecondTour = new VBox(10);
        panelSecondTour.getStyleClass().add("panel");
        Button btnCalculerSecond = new Button("Calculer résultats second tour");
        btnCalculerSecond.setOnAction(e -> calculerSecondTour());
        panelSecondTour.getChildren().addAll(
                new Label("Second Tour - Saisie des suffrages:"),
                btnCalculerSecond
        );
        mainLayout.getChildren().add(panelSecondTour);
        panelSecondTour.setVisible(false);
    }

    private void ajouterCandidat() {
        String nom = tfNomCandidat.getText().trim();
        if (!nom.isEmpty() && candidats.stream().noneMatch(c -> c.getNom().equalsIgnoreCase(nom))) {
            Candidat nouveau = new Candidat(nom);
            candidats.add(nouveau);
            DatabaseManager.sauvegarderCandidat(nouveau);
            tfNomCandidat.clear();
        } else {
            afficherAlerte("Nom invalide ou déjà existant !");
        }
    }

    private void validerVotants() {
        try {
            totalVotants = Integer.parseInt(tfVotants.getText());
            if(totalVotants > 0) {
                panelPremierTour.setVisible(true);
                btnValiderVotants.setDisable(true);
            }
        } catch (NumberFormatException e) {
            afficherAlerte("Nombre de votants invalide !");
        }
    }

    private void calculerPremierTour() {
        // Récupérer les votes depuis l'interface
        List<Candidat> candidatsAvecVotes = new ArrayList<>();
        GridPane grid = (GridPane) panelPremierTour.getChildren().get(1);

        int row = 0;
        for(Node node : grid.getChildren()) {
            if(node instanceof TextField) {
                int votes = Integer.parseInt(((TextField) node).getText());
                candidats.get(row).setSuffragesPremierTour(votes);
                row++;
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