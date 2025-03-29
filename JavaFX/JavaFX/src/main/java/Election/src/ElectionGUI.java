package Election.src;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ElectionGUI extends Application {
    private ObservableList<Candidat> candidats = FXCollections.observableArrayList();
    private int nombreCandidats;
    private int totalVotants;

    @Override
    public void start(Stage primaryStage) {
        demanderNombreCandidats(primaryStage);
    }

    // ================== MÉTHODES DU FLUX PRINCIPAL ==================
    private void demanderNombreCandidats(Stage stage) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Configuration des élections");
        dialog.setHeaderText("Nombre de candidats :");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(response -> {
            try {
                nombreCandidats = Integer.parseInt(response);
                if(nombreCandidats < 2) throw new NumberFormatException();
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
                    demanderNomsCandidats(index + 1, noms, stage);
                }
            });
        } else {
            creerCandidats(noms);
            demanderTotalVotants(stage);
        }
    }

    private void demanderTotalVotants(Stage stage) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Participation électorale");
        dialog.setHeaderText("Nombre total de votants :");

        dialog.showAndWait().ifPresent(response -> {
            try {
                totalVotants = Integer.parseInt(response);
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
                    demanderVoixCandidats(index + 1, stage);
                }
                catch (NumberFormatException e) {
                    afficherErreur("Valeur invalide ! Doit être entre 0 et " + totalVotants);
                    demanderVoixCandidats(index, stage);
                }
            });
        } else {
            afficherInterfaceFinale(stage);
        }
    }

    // ================== LOGIQUE MÉTIER ==================
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

    // ================== INTERFACE & BASE DE DONNÉES ==================
    private void afficherInterfaceFinale(Stage stage) {
        Button btn = new Button("Calculer les résultats");
        btn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText("Voulez-vous procéder au calcul ?");
            confirm.showAndWait().ifPresent(response -> {
                if(response == ButtonType.OK) determinerResultats();
            });
        });

        VBox root = new VBox(20, btn);
        Scene scene = new Scene(root, 400, 300);
        stage.setScene(scene);
        stage.show();
    }

    private void mettreAJourBDD(Candidat c) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "UPDATE candidat SET suffrages_premier_tour = ? WHERE nom = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, c.getSuffragesPremierTour());
            pstmt.setString(2, c.getNom());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    // ================== UTILITAIRES ==================
    private void creerCandidats(List<String> noms) {
        noms.forEach(nom -> {
            Candidat c = new Candidat(nom);
            candidats.add(c);
            ajouterCandidatBDD(c);
        });
    }

    private void ajouterCandidatBDD(Candidat c) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "INSERT INTO candidat (nom) VALUES (?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, c.getNom());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void afficherErreur(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    private void afficherResultat(String message) {
        TextArea area = new TextArea(message);
        area.setEditable(false);
        VBox box = new VBox(area);
        Scene scene = new Scene(box, 500, 300);

        Stage stage = new Stage();
        stage.setTitle("Résultats détaillés");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}