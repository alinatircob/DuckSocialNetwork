package org.example.ducksocialnetworkm.factory;
import org.example.ducksocialnetworkm.domeniu.user.*;

/**
 * Clasa {@code DuckFactory} implementează modelul Factory pentru crearea obiectelor de tip {@link Rata}.
 * Creează instanțe de rațe (zburătoare, înotătoare sau mixte) în funcție de tipul specificat.
 */
public class DuckFactory {

    /**
     * Creează o rață de un anumit tip.
     *
     * @param id identificatorul raței
     * @param username numele de utilizator
     * @param email adresa de email
     * @param password parola
     * @param rezistenta valoarea rezistenței fizice
     * @param viteza viteza maximă
     * @param tip tipul raței — poate fi "FLYING", "SWIMMING" sau "FLYING_AND_SWIMMING"
     * @return o instanță de {@link Rata} corespunzătoare tipului specificat
     * @throws IllegalArgumentException dacă tipul raței este invalid
     */
    public static Rata creeazaRata(Long id, String username, String email, String password,
                                   Double rezistenta, Double viteza, String tip) {

        if (tip == null) {
            throw new IllegalArgumentException("Tipul ratei nu poate fi null.");
        }

        tip = tip.trim().toUpperCase(); // FIX CRITIC !!!

        TipRata tipRata;
        try {
            tipRata = TipRata.valueOf(tip);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tip invalid pentru rata: " + tip);
        }

        switch (tipRata) {
            case SWIMMING:
                return new SwimmingDuck(new RataSimpla(id, username, email, password, rezistenta, viteza, tipRata));

            case FLYING:
                return new FlyingDuck(new RataSimpla(id, username, email, password, rezistenta, viteza, tipRata));

            default:
                return new SwimmingDuck(new FlyingDuck(
                        new RataSimpla(id, username, email, password, rezistenta, viteza, tipRata)
                ));
        }
    }

}