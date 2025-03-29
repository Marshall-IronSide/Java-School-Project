package Election.src;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ElectionGUI extends Application {
    private ObservableList<Candidat> candidats = FXCollections.observableArrayList();
    private int nombreCandidats;
    private int totalVotants;
    private TableView<Candidat> tableCandidats;
    private Label statusLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Système de Gestion Électorale");
        setupMainInterface(primaryStage);
        demanderNombreCandidats(primaryStage);
    }

    private void setupMainInterface(Stage stage) {
        tableCandidats = new TableView<>();
        setupTableColumns();

        Button btnCalcul = new Button("Calculer les Résultats");
        btnCalcul.setStyle("-fx-base: #2ecc71;");
        btnCalcul.setOnAction(e -> confirmerCalcul());

        Button btnRafraichir = new Button("Rafraîchir les Données");
        btnRafraichir.setOnAction(e -> rafraichirTableau());

        ToolBar toolBar = new ToolBar(
                new Label("Actions: "),
                btnCalcul,
                btnRafraichir
        );

        statusLabel = new Label("Prêt");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        statusLabel.setTextFill(Color.DARKGRAY);

        BorderPane root = new BorderPane();
        root.setTop(createHeader());
        root.setCenter(createMainContent());
        root.setBottom(createStatusBar());

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    private HBox createHeader() {
        Label title = new Label("Élections 2024");
        title.setFont(Font.font("Tahoma", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);

        HBox header = new HBox(title);
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: #2980b9; -fx-padding: 20;");
        return header;
    }

    private VBox createMainContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        GridPane configPanel = new GridPane();
        configPanel.setHgap(10);
        configPanel.setVgap(10);
        configPanel.add(new Label("Nombre de candidats:"), 0, 0);
        TextField nbCandidatsField = new TextField();
        configPanel.add(nbCandidatsField, 1, 0);

        VBox resultsSection = new VBox(10);
        resultsSection.getChildren().addAll(
                new Label("Résultats en temps réel:"),
                tableCandidats
        );

        content.getChildren().addAll(configPanel, resultsSection);
        return content;
    }

    private HBox createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setPadding(new Insets(10));
        statusBar.setStyle("-fx-background-color: #ecf0f1;");
        statusBar.getChildren().add(statusLabel);
        return statusBar;
    }

    private void setupTableColumns() {
        TableColumn<Candidat, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));

        TableColumn<Candidat, Integer> voix1Col = new TableColumn<>("Voix (1er tour)");
        voix1Col.setCellValueFactory(new PropertyValueFactory<>("suffragesPremierTour"));

        TableColumn<Candidat, Integer> voix2Col = new TableColumn<>("Voix (2nd tour)");
        voix2Col.setCellValueFactory(new PropertyValueFactory<>("suffragesSecondTour"));

        tableCandidats.getColumns().setAll(nomCol, voix1Col, voix2Col);
        tableCandidats.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // MÉTHODES MANQUANTES AJOUTÉES

    private void demanderNombreCandidats(Stage stage) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Configuration des élections");
        dialog.setHeaderText("Nombre de candidats :");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(response -> {
            try {
                nombreCandidats = Integer.parseInt(response);
                if(nombreCandidats < 2) throw new NumberFormatException();
                statusLabel.setText("Saisie des noms des candidats...");
                demanderNomsCandidats(0, new ArrayList<>(), stage);
            }
            catch (NumberFormatException e) {
                afficherErreur("Nombre invalide ! Minimum 2 candidats.");
                demanderNombreCandidats(stage);
            }
        });
    }

    private void demanderNomsCandidats(int index, List<String> noms, Stage stage) {
        if(index < nombreCandidats) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Candidat " + (index + 1));
            dialog.setHeaderText("Nom du candidat " + (index + 1) + " :");

            dialog.showAndWait().ifPresent(nom -> {
                if(nom.isEmpty()) {
                    afficherErreur("Le nom ne peut pas être vide !");
                    demanderNomsCandidats(index, noms, stage);
                } else {
                    noms.add(nom);
                    statusLabel.setText("Candidat " + (index+1) + "/" + nombreCandidats + " enregistré");
                    demanderNomsCandidats(index + 1, noms, stage);
                }
            });
        } else {
            creerCandidats(noms);
            demanderTotalVotants(stage);
        }
    }

    private void creerCandidats(List<String> noms) {
        noms.forEach(nom -> {
            Candidat c = new Candidat(nom);
            candidats.add(c);
            ajouterCandidatBDD(c);
        });
        tableCandidats.setItems(candidats);
    }

    private void demanderTotalVotants(Stage stage) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Participation électorale");
        dialog.setHeaderText("Nombre total de votants :");

        dialog.showAndWait().ifPresent(response -> {
            try {
                totalVotants = Integer.parseInt(response);
                statusLabel.setText("Saisie des voix des candidats...");
                demanderVoixCandidats(0, stage);
            }
            catch (NumberFormatException e) {
                afficherErreur("Nombre invalide !");
                demanderTotalVotants(stage);
            }
        });
    }

    private void demanderVoixCandidats(int index, Stage stage) {
        if(index < nombreCandidats) {
            Candidat candidat = candidats.get(index);
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Suffrages pour " + candidat.getNom());
            dialog.setHeaderText("Voix obtenues par " + candidat.getNom() + " :");

            dialog.showAndWait().ifPresent(voixStr -> {
                try {
                    int voix = Integer.parseInt(voixStr);
                    if(voix < 0 || voix > totalVotants) throw new NumberFormatException();

                    candidat.setSuffragesPremierTour(voix);
                    mettreAJourBDD(candidat);
                    tableCandidats.refresh();
                    demanderVoixCandidats(index + 1, stage);
                }
                catch (NumberFormatException e) {
                    afficherErreur("Valeur invalide ! Doit être entre 0 et " + totalVotants);
                    demanderVoixCandidats(index, stage);
                }
            });
        } else {
            statusLabel.setText("Saisie terminée - Prêt pour calcul");
        }
    }

    private void determinerResultats() {
        StringBuilder resultats = new StringBuilder("=== RÉSULTATS DU PREMIER TOUR ===\n");
        boolean victoire = false;

        for(Candidat c : candidats) {
            double pourcentage = (c.getSuffragesPremierTour() * 100.0) / totalVotants;
            resultats.append(String.format("- %s : %d voix (%.2f%%)\n",
                    c.getNom(), c.getSuffragesPremierTour(), pourcentage));

            if(pourcentage > 50.0) {
                resultats.append("\n➤ VICTOIRE DE ").append(c.getNom()).append(" AU PREMIER TOUR !");
                afficherResultat(resultats.toString());
                victoire = true;
                break;
            }
        }

        if(!victoire) {
            resultats.append("\n➤ SECOND TOUR NÉCESSAIRE !");
            afficherResultat(resultats.toString());
            preparerSecondTour();
        }
    }

    private void preparerSecondTour() {
        candidats.sort((c1, c2) -> Integer.compare(c2.getSuffragesPremierTour(), c1.getSuffragesPremierTour()));
        List<Candidat> finalistes = candidats.subList(0, 2);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Second tour");
        alert.setHeaderText("Les finalistes sont :\n" +
                finalistes.get(0).getNom() + " et " + finalistes.get(1).getNom());
        alert.showAndWait();

        demanderVoixSecondTour(finalistes);
    }

    private void demanderVoixSecondTour(List<Candidat> finalistes) {
        finalistes.forEach(c -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Second tour - " + c.getNom());
            dialog.setHeaderText("Voix pour " + c.getNom() + " :");

            dialog.showAndWait().ifPresent(voixStr -> {
                try {
                    int voix = Integer.parseInt(voixStr);
                    c.setSuffragesSecondTour(voix);
                    mettreAJourSecondTourBDD(c);
                    tableCandidats.refresh();
                }
                catch (NumberFormatException e) {
                    afficherErreur("Valeur invalide !");
                }
            });
        });

        Candidat vainqueur = finalistes.get(0).getSuffragesSecondTour() > finalistes.get(1).getSuffragesSecondTour()
                ? finalistes.get(0) : finalistes.get(1);

        afficherResultat("VAINQUEUR FINAL : " + vainqueur.getNom() +
                " (" + vainqueur.getSuffragesSecondTour() + " voix)");
    }

    private void mettreAJourBDD(Candidat c) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "UPDATE candidat SET suffrages_premier_tour = ? WHERE nom = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, c.getSuffragesPremierTour());
            pstmt.setString(2, c.getNom());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            afficherErreur("Erreur de base de données : " + e.getMessage());
        }
    }

    private void mettreAJourSecondTourBDD(Candidat c) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "UPDATE candidat SET suffrages_second_tour = ? WHERE nom = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, c.getSuffragesSecondTour());
            pstmt.setString(2, c.getNom());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            afficherErreur("Erreur de base de données : " + e.getMessage());
        }
    }

    private void ajouterCandidatBDD(Candidat c) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "INSERT INTO candidat (nom) VALUES (?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, c.getNom());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            afficherErreur("Erreur de base de données : " + e.getMessage());
        }
    }

    private void afficherErreur(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    // MÉTHODES EXISTANTES COMPLÉTÉES

    private void rafraichirTableau() {
        tableCandidats.refresh();
        statusLabel.setText("Données mises à jour - " + java.time.LocalDateTime.now().toString());
    }

    private void confirmerCalcul() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Voulez-vous procéder au calcul ?");
        confirm.showAndWait().ifPresent(response -> {
            if(response == ButtonType.OK) determinerResultats();
        });
    }

    private void afficherGraphiqueResultats() {
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Répartition des voix (1er tour)");

        for (Candidat c : candidats) {
            PieChart.Data slice = new PieChart.Data(
                    c.getNom() + " (" + c.getSuffragesPremierTour() + ")",
                    c.getSuffragesPremierTour()
            );
            pieChart.getData().add(slice);
        }

        Stage chartStage = new Stage();
        chartStage.setTitle("Visualisation des résultats");
        Scene scene = new Scene(pieChart, 600, 400);
        chartStage.setScene(scene);
        chartStage.show();
    }

    private void afficherResultat(String message) {
        TextArea area = new TextArea(message);
        area.setEditable(false);
        area.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 14px;");

        VBox box = new VBox(20, area);
        box.setPadding(new Insets(20));

        Stage stage = new Stage();
        stage.setTitle("Résultats détaillés");
        stage.setScene(new Scene(box, 500, 300));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}