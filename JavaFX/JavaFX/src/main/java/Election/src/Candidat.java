package Election.src;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Candidat {
    private final StringProperty nom = new SimpleStringProperty();
    private final IntegerProperty suffragesPremierTour = new SimpleIntegerProperty();
    private final IntegerProperty suffragesSecondTour = new SimpleIntegerProperty();

    public Candidat(String nom) {
        this.nom.set(nom);
    }

    // === Getters/Setters pour les propriétés ===

    // Nom
    public StringProperty nomProperty() { return nom; }
    public String getNom() { return nom.get(); }
    public void setNom(String nom) { this.nom.set(nom); }

    // Suffrages Premier Tour
    public IntegerProperty suffragesPremierTourProperty() { return suffragesPremierTour; }
    public int getSuffragesPremierTour() { return suffragesPremierTour.get(); }
    public void setSuffragesPremierTour(int voix) { suffragesPremierTour.set(voix); }

    // Suffrages Second Tour
    public IntegerProperty suffragesSecondTourProperty() { return suffragesSecondTour; }
    public int getSuffragesSecondTour() { return suffragesSecondTour.get(); }
    public void setSuffragesSecondTour(int voix) { suffragesSecondTour.set(voix); }
}