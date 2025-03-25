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
        for (Candidat c: candidats){
            System.out.print("voix pour : "+c.getNom());
            int voix = scanner.nextInt();
            c.setSuffragesPremierTour(voix);
        }
    }
    public Map<Candidat, Double> calculerPourcentages(){
        Map<Candidat, Double> pourcentages = new HashMap<>();
        for (Candidat c: candidats){
            double pourcentage = (c.getSuffragesPremierTour()*100.0)/totalVotants;
            pourcentages.put(c, pourcentage);
        }
        return pourcentages;
    }
    public void determinerResultatsPremierTour(){
        for (Candidat c: candidats){
            double pourcentage = (c.getSuffragesPremierTour()*100.0)/totalVotants;
            if (pourcentage > 50.0){
                System.out.println("Vainqueur au premier tour : "+ c.getNom());
                return;
            }
        }
    }
    public void gererResultatsSecondTour(){
        List<Candidat> SecondTour = candidats.subList(0, 2);

        System.out.println("--- SECOND TOUR ---");
        for (Candidat c: SecondTour){
            System.out.println("Voix pour "+c.getNom()+": ");
            int voix = scanner.nextInt();
            c.setSuffragesSecondTour(voix);
        }
        Candidat vainqueur = (secondTour.get(0).getSuffragesSecondTour() > secondTour.get(1).getStuffragesSecondtoure());
    }
}
