package Election.src;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class ElectionGUI extends Application {
    private VBox mainLayout;
    private TextField tfNomCandidat;
    private Button btnAjouter;
    private ListView<Candidat> listCandidats;
    private TextField tfVotants;
    private Button btnValiderVotants;

    @Override
    public void start(Stage primaryStage) {
        DatabaseManager.initializeDatabase();

        mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(20));
        mainLayout.getStyleClass().add("main-pane");

        // Section saisie candidats
        HBox saisieBox = new HBox(10);
        tfNomCandidat = new TextField();
        btnAjouter = new Button("Ajouter Candidat");
        saisieBox.getChildren().addAll(tfNomCandidat, btnAjouter);

        // Liste des candidats
        listCandidats = new ListView<>();
        listCandidats.getStyleClass().add("candidat-list");

        // Section votants
        HBox votantsBox = new HBox(10);
        tfVotants = new TextField();
        btnValiderVotants = new Button("Valider nombre votants");
        votantsBox.getChildren().addAll(new Label("Total votants:"), tfVotants, btnValiderVotants);

        mainLayout.getChildren().addAll(
                new Label("Gestion des Élections"),
                saisieBox,
                listCandidats,
                votantsBox
        );

        // Gestion des événements
        btnAjouter.setOnAction(e -> ajouterCandidat());
        btnValiderVotants.setOnAction(e -> validerVotants());

        Scene scene = new Scene(mainLayout, 600, 400);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        primaryStage.setTitle("Gestion des Élections");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void ajouterCandidat() {
        String nom = tfNomCandidat.getText();
        if (!nom.isEmpty()) {
            Candidat nouveau = new Candidat(nom);
            listCandidats.getItems().add(nouveau);
            DatabaseManager.sauvegarderCandidat(nouveau);
            tfNomCandidat.clear();
        }
    }

    private void validerVotants() {
        // Implémentation de la validation et du passage aux votes
    }

    public static void main(String[] args) {
        launch(args);
    }
}