import java.util.Scanner;
public class Main {
    public static void main(String[] args) {
        DatabaseManager.creerBaseDeDonnees();
        Election election = new Election();
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== GESTION DES ÉLECTIONS ===");
        election.saisirCandidats(scanner);
        election.saisirSuffragesPremierTour(scanner);
        election.determinerResultatsPremierTour();

        System.out.println("Voulez-vous procéder au second tour ? (Oui/Non)");
        if (scanner.next().equalsIgnoreCase("Oui")){
            election.gererSecondTour(scanner);
        }
        scanner.close();
    }
}