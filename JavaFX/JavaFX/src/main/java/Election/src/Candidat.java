package Election.src;

public class Candidat {
    private int id;
    private String nom;
    private int suffragesPremierTour;
    private int suffragesSecondTour;
    private boolean qualifieSecondTour;

    // Constructeurs
    public Candidat() {
    }

    public Candidat(String nom) {
        this.nom = nom;
        this.suffragesPremierTour = 0;
        this.suffragesSecondTour = 0;
        this.qualifieSecondTour = false;
    }

    public Candidat(int id, String nom, int suffragesPremierTour, int suffragesSecondTour, boolean qualifieSecondTour) {
        this.id = id;
        this.nom = nom;
        this.suffragesPremierTour = suffragesPremierTour;
        this.suffragesSecondTour = suffragesSecondTour;
        this.qualifieSecondTour = qualifieSecondTour;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public int getSuffragesPremierTour() {
        return suffragesPremierTour;
    }

    public int getSuffragesSecondTour() {
        return suffragesSecondTour;
    }

    public boolean isQualifieSecondTour() {
        return qualifieSecondTour;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setSuffragesPremierTour(int suffragesPremierTour) {
        this.suffragesPremierTour = suffragesPremierTour;
    }

    public void setSuffragesSecondTour(int suffragesSecondTour) {
        this.suffragesSecondTour = suffragesSecondTour;
    }

    public void setQualifieSecondTour(boolean qualifieSecondTour) {
        this.qualifieSecondTour = qualifieSecondTour;
    }

    // Méthode utilitaire pour calculer le pourcentage
    public double calculerPourcentagePremierTour(int totalVotants) {
        if (totalVotants == 0) return 0.0;
        return (suffragesPremierTour * 100.0) / totalVotants;
    }

    public double calculerPourcentageSecondTour(int totalVotants) {
        if (totalVotants == 0) return 0.0;
        return (suffragesSecondTour * 100.0) / totalVotants;
    }

    @Override
    public String toString() {
        return nom + " (" + suffragesPremierTour + " votes)";
    }
}