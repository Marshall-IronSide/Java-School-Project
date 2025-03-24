public class Candidat {
    String nom;
    int suffragesPremierTour;
    int suffragesSecondTour;
    public Candidat(String nom){
        this.nom=nom;
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
    public void setNom(String nom) {
        this.nom = nom;
    }
    public void setSuffragesPremierTour(int suffragesPremierTour) {
        this.suffragesPremierTour = suffragesPremierTour;
    }
    public void setSuffragesSecondTour(int suffragesSecondTour) {
        this.suffragesSecondTour = suffragesSecondTour;
    }
}
