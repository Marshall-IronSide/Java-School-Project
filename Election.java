import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Election {
    List<Candidat> candidats;
    int totalVotants;
    public void saisirCandidats(Scanner scanner){
        System.out.println("Nombre de candidats: ");
        int nbCandidats = scanner.nextInt();
        scanner.nextLine();// Pour consommer le "\n"
        for (int i = 0; i < nbCandidats; i++) {
            System.out.println("Nom du candidat: "+(i+1)+":");
            String nom = scanner.nextLine();
            candidats.add(new Candidat(nom));
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
        Map<Candidat, Double>
    }
}
