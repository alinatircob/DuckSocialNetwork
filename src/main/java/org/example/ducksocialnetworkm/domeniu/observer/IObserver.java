package org.example.ducksocialnetworkm.domeniu.observer;

/**
 * Interfață care definește comportamentul unui observator (observer).
 * Un observator este notificat de obiectul observabil atunci când apare un eveniment.
 */

public interface IObserver {

    /**
     * Metodă apelată de obiectul observabil pentru a trimite o notificare observatorului.
     * @param mesaj mesajul primit de la obiectul observabil
     */
    void update(String mesaj);

}
