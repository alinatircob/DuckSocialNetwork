package org.example.ducksocialnetworkm.domeniu.card;


import org.example.ducksocialnetworkm.domeniu.user.Rata;

import java.util.ArrayList;
import java.util.List;

/**
 * Clasa abstractă {@code Card} reprezintă un card generic asociat unui anumit tip de rață.
 * Cardul conține informații de bază (ID, nume, tip) și lista rațelor asociate lui.
 * @param <T> tipul de rață (extinde {@link Rata})
 */

public abstract class Card<T extends Rata> {
    private Long id;
    private String nume;
    private List<T> rate;
    private String tip;


    /**
     * Creează un card nou.
     * @param id    identificatorul unic al cardului
     * @param nume  numele cardului
     * @param tip   tipul cardului (ex. "Inotator", "Zburator")
     */
    public Card(Long id, String nume, String tip) {
        this.id = id;
        this.nume = nume;
        this.rate = new ArrayList<>();
        this.tip = tip;
    }


    /**
     * Calculează performanța medie a rațelor de pe card.
     * Formula: suma(viteze) / suma(rezistențe).
     * @return valoarea performanței medii (0.0 dacă nu există rațe)
     */
    public double getPerformantaMedie() {
        if (rate.isEmpty()) return 0.0;
        double viteze = 0;
        double rezistente = 0;
        for (Rata rata : rate) {
            viteze += rata.getViteza();
            rezistente += rata.getRezistenta();
        }
        return viteze / rezistente;
    }

    /** @return lista de rațe asociate cardului */
    public List<T> getRate() { return rate; }

    /** @return numele cardului */
    public String getNume() { return nume; }

    /** @return ID-ul cardului */
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public void setNume(String nume) { this.nume = nume; }

    public void setRate(List<T> rate) { this.rate = rate; }

    /**
     * Adaugă o rață pe card.
     * @param rata rața de adăugat
     */
    public void adaugaRata(T rata) {
        this.rate.add(rata);
    }

    /**
     * Șterge o rață de pe card.
     * @param rata rața de șters
     */
    public void stergeRata(T rata) {
        this.rate.remove(rata);
    }

    /** @return tipul cardului (ex. „Inotator”, „Zburator”) */
    public String getTip() {
        return tip;
    }

    @Override
    public String toString() {
        return id + ";"  + nume;
    }

}

