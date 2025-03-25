public class Candidat {
    String nom;
    int suffragesPremierTour;
    int suffragesSecondTour;
    public Candidat(String nom){
        this.nom=nom;
        this.suffragesPremierTour=0;
        this.suffragesSecondTour=0;
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
    public void setSuffragesPremierTour(int voix) {
        this.suffragesPremierTour = voix;
    }
    public void setSuffragesSecondTour(int voix) {
        this.suffragesSecondTour = voix;
    }
}
