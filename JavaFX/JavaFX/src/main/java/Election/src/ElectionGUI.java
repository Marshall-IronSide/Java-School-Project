package Election.src;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.*;

public class ElectionGUI extends Application {
    private ObservableList<Candidat> candidats = FXCollections.observableArrayList();
    private TableView<Candidat> table = new TableView<>();

    @Override
    public void start(Stage primaryStage) {
        DatabaseManager.creerBaseDeDonnees();
        chargerCandidatsDepuisBDD();

        // Configuration de la TableView
        TableColumn<Candidat, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(cellData -> cellData.getValue().nomProperty());

        TableColumn<Candidat, Number> voix1Col = new TableColumn<>("Voix 1er tour");
        voix1Col.setCellValueFactory(cellData -> cellData.getValue().suffragesPremierTourProperty());

        TableColumn<Candidat, Number> voix2Col = new TableColumn<>("Voix 2nd tour");
        voix2Col.setCellValueFactory(cellData -> cellData.getValue().suffragesSecondTourProperty());

        table.getColumns().addAll(nomCol, voix1Col, voix2Col);
        table.setItems(candidats);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Formulaire de saisie
        TextField txtNom = new TextField();
        Button btnAjouter = new Button("Ajouter");
        Button btnSupprimer = new Button("Supprimer");
        Button btnCalculer = new Button("Calculer");

        btnAjouter.setOnAction(e -> ajouterCandidat(txtNom.getText()));
        btnCalculer.setOnAction(e -> gererCalcul());

        btnSupprimer.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");

        btnAjouter.setOnAction(e -> ajouterCandidat(txtNom.getText()));
        btnSupprimer.setOnAction(e -> supprimerCandidatSelectionne());
        btnCalculer.setOnAction(e -> gererCalcul());

        // Layout
        HBox hbox = new HBox(10, new Label("Entrez le nom du candidat :"), txtNom, btnAjouter, btnSupprimer);
        VBox vbox = new VBox(20, hbox, table, btnCalculer);

        vbox.setPadding(new Insets(15));

        primaryStage.setScene(new Scene(vbox, 600, 400));
        primaryStage.setTitle("Gestion des Élections");
        primaryStage.show();
    }

    private void ajouterCandidat(String nom) {
        if (!nom.isEmpty()) {
            try (Connection conn = DatabaseManager.getConnection()) {
                String sql = "INSERT INTO candidat (nom) VALUES (?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, nom);
                pstmt.executeUpdate();
                candidats.add(new Candidat(nom));
            } catch (SQLException e) {
                showAlert("Erreur : Ce candidat existe déjà !");
            }
        }
    }
    private void supprimerCandidatSelectionne() {
        Candidat selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                DatabaseManager.supprimerCandidat(selected.nomProperty().get());
                candidats.remove(selected); // Retire de l'affichage
                showAlert(selected.nomProperty().get() + " supprimé !");
            } catch (SQLException ex) {
                showAlert("Erreur SQL : " + ex.getMessage());
            }
        } else {
            showAlert("Aucun candidat sélectionné !");
        }
    }

    private void gererCalcul() {
        try {
            int totalVotants = Integer.parseInt(showInputDialog("Nombre total de votants :"));
            saisirVoixPremierTour(totalVotants);
        } catch (NumberFormatException e) {
            showAlert("Erreur : Entrez un nombre valide !");
        }
    }

    private void saisirVoixPremierTour(int totalVotants) {
        for (Candidat c : candidats) {
            try {
                int voix = Integer.parseInt(showInputDialog("Voix pour " + c.nomProperty().get()));
                if (voix > totalVotants) throw new IllegalArgumentException();
                c.suffragesPremierTourProperty().set(voix);
                mettreAJourBDD(c);
            } catch (NumberFormatException e) {
                showAlert("Erreur : Entrez un nombre valide !");
            }
        }
        determinerResultats();
    }

    private void chargerCandidatsDepuisBDD() {
        try (Connection conn = DatabaseManager.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM candidat");
            while (rs.next()) {
                Candidat c = new Candidat(rs.getString("nom"));
                c.suffragesPremierTourProperty().set(rs.getInt("suffrages_premier_tour"));
                candidats.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void mettreAJourBDD(Candidat c) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "UPDATE candidat SET suffrages_premier_tour = ? WHERE nom = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, c.suffragesPremierTourProperty().get());
            pstmt.setString(2, c.nomProperty().get());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void determinerResultats() {
        // Calcul du total des votants
        int totalVotants = candidats.stream()
                .mapToInt(c -> c.suffragesPremierTourProperty().get())
                .sum();

        // Vérifier si un candidat a >50% au premier tour
        boolean victoirePremierTour = false;
        StringBuilder resultats = new StringBuilder("RÉSULTATS DU PREMIER TOUR :\n");

        for (Candidat c : candidats) {
            double pourcentage = (c.suffragesPremierTourProperty().get() * 100.0) / totalVotants;
            resultats.append(String.format("- %s : %d voix (%.2f%%)\n",
                    c.nomProperty().get(),
                    c.suffragesPremierTourProperty().get(),
                    pourcentage));

            if (pourcentage > 50.0) {
                showAlert(resultats + "\nVICTOIRE AU PREMIER TOUR : " + c.nomProperty().get());
                victoirePremierTour = true;
                return;
            }
        }

        // Si second tour nécessaire
        resultats.append("\n➤ SECOND TOUR REQUIS !");
        showAlert(resultats.toString());
        lancerSecondTour();
    }

    private void lancerSecondTour() {
        // Sélectionner les 2 premiers candidats
        candidats.sort((c1, c2) -> c2.suffragesPremierTourProperty().get() - c1.suffragesPremierTourProperty().get());
        ObservableList<Candidat> secondTour = FXCollections.observableArrayList(
                candidats.get(0),
                candidats.get(1)
        );

        // Fenêtre pour saisir les voix du second tour
        Stage stageSecondTour = new Stage();
        VBox vbox = new VBox(10);

        for (Candidat c : secondTour) {
            TextField txtVoix = new TextField();
            txtVoix.setPromptText("Voix pour " + c.nomProperty().get());
            vbox.getChildren().add(txtVoix);
        }

        Button btnValider = new Button("Valider");
        btnValider.setOnAction(e -> {
            for (int i = 0; i < secondTour.size(); i++) {
                TextField txt = (TextField) vbox.getChildren().get(i);
                int voix = Integer.parseInt(txt.getText());
                secondTour.get(i).suffragesSecondTourProperty().set(voix);
                mettreAJourSecondTourBDD(secondTour.get(i));
            }
            afficherVainqueurSecondTour(secondTour);
            stageSecondTour.close();
        });

        vbox.getChildren().add(btnValider);
        stageSecondTour.setScene(new Scene(vbox, 300, 200));
        stageSecondTour.show();
    }

    private void afficherVainqueurSecondTour(ObservableList<Candidat> secondTour) {
        Candidat vainqueur = (secondTour.get(0).suffragesSecondTourProperty().get() >
                secondTour.get(1).suffragesSecondTourProperty().get())
                ? secondTour.get(0) : secondTour.get(1);

        showAlert("VAINQUEUR AU SECOND TOUR :\n" +
                vainqueur.nomProperty().get() + " (" +
                vainqueur.suffragesSecondTourProperty().get() + " voix)");
    }

    private void mettreAJourSecondTourBDD(Candidat c) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "UPDATE candidat SET suffrages_second_tour = ? WHERE nom = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, c.suffragesSecondTourProperty().get());
            pstmt.setString(2, c.nomProperty().get());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Méthodes utilitaires
    private String showInputDialog(String message) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setHeaderText(message);
        return dialog.showAndWait().orElse("");
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}