package Election.src;

import javafx.beans.property.*;

public class Candidat {
    private final StringProperty nom = new SimpleStringProperty();
    private final IntegerProperty suffragesPremierTour = new SimpleIntegerProperty();
    private final IntegerProperty suffragesSecondTour = new SimpleIntegerProperty();

    public Candidat(String nom) {
        this.nom.set(nom);
    }

    // Getters/Setters pour JavaFX
    public StringProperty nomProperty() { return nom; }
    public IntegerProperty suffragesPremierTourProperty() { return suffragesPremierTour; }
    public IntegerProperty suffragesSecondTourProperty() { return suffragesSecondTour; }
    public String getNom() { return nom.get(); }
    public int getSuffragesPremierTour() { return suffragesPremierTour.get(); }
    public int getSuffragesSecondTour() { return suffragesSecondTour.get(); }

}
