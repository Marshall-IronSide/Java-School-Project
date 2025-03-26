import java.util.*;
import java.sql.*;

public class Election {
    List<Candidat> candidats = new ArrayList<>();
    private int totalVotants;

    public void saisirCandidats(Scanner scanner){
        System.out.println("Nombre de candidats: ");
        int nbCandidats = scanner.nextInt();
        scanner.nextLine();// Pour consommer le "\n"

        try (Connection conn = DatabaseManager.getConnection()){
            for (int i = 0; i < nbCandidats; i++) {
                System.out.println("Nom du candidat: "+(i+1)+":");
                String nom = scanner.nextLine();

                String sql = "INSERT INTO candidat (nom) VALUES (?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)){
                    pstmt.setString(1, nom);
                    pstmt.executeUpdate();
                }
                candidats.add(new Candidat(nom));
            }
        }catch (SQLException e){
            System.out.println("Erreur SQL: "+ e.getMessage());
        }

    }
    public void saisirSuffragesPremierTour(Scanner scanner){
        System.out.println("Nombre total de votants: ");
        this.totalVotants = scanner.nextInt();

        try (Connection conn = DatabaseManager.getConnection()){
            for (Candidat c: candidats){
                System.out.print("voix pour : "+c.getNom()+"\n");
                int voix = scanner.nextInt();

                String sql = "UPDATE candidat SET suffrages_premier_tour = ? WHERE nom = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)){
                    pstmt.setInt(1,voix);
                    pstmt.setString(2, c.getNom());
                    pstmt.executeUpdate();
                }
                c.setSuffragesPremierTour(voix);
            }
        }catch (SQLException e){
            System.out.println("Erreur SQL: "+ e.getMessage());
        }
    }
    public void determinerResultatsPremierTour(){
        candidats.sort((c1,c2) -> c2.getSuffragesPremierTour() - c1.getSuffragesPremierTour());
        for (Candidat c: candidats){
            double pourcentage = (c.getSuffragesPremierTour()*100.0)/totalVotants;
            if (pourcentage > 50.0){
                System.out.println(c.getNom() + " élu au premier tour avec " + String.format("%.2f", pourcentage) + "%");
                return;
            }
        }
        System.out.println("Second tour nécessaire !");
        List<Candidat> secondTour = candidats.subList(0,2);
        System.out.println("Candidats en second tour : " + secondTour.get(0).getNom() + " et " + secondTour.get(1).getNom());
    }
    public void gererSecondTour(Scanner scanner){
        List<Candidat> secondTour = candidats.subList(0, 2);
        System.out.println("--- SECOND TOUR ---");

        try (Connection conn = DatabaseManager.getConnection()){
            for (Candidat c: secondTour){
                System.out.println("Voix pour "+c.getNom()+": ");
                int voix = scanner.nextInt();

                String sql = "UPDATE candidat SET suffrages_second_tour = ? WHERE nom = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)){
                    pstmt.setInt(1,voix);
                    pstmt.setString(2, c.getNom());
                    pstmt.executeUpdate();
                }
                c.setSuffragesSecondTour(voix);
            }
            Candidat vainqueur = (secondTour.get(0).getSuffragesSecondTour() > secondTour.get(1).getSuffragesSecondTour())
                    ? secondTour.get(0) : secondTour.get(1);
            System.out.println("Vainqueur : " + vainqueur.getNom());
        }catch (SQLException e){
            System.out.println("Erreur SQL: "+ e.getMessage());
        }
    }

}
