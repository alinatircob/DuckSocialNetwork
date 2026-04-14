package org.example.ducksocialnetworkm.depozit;

import java.io.IOException;
import java.util.Collection;

/**
 * Interfața {@code Depozit} definește operațiile de bază pentru manipularea persistentă a datelor.
 * Toate clasele care o implementează trebuie să asigure citirea și scrierea datelor dintr-un fișier.
 */

public interface Depozit<T> {

    /**
     * Adaugă un element nou în depozit.
     *
     * @param var obiectul care va fi adăugat
     */
    void adauga(T var);

    /**
     * Șterge un element din depozit, identificat printr-unul sau mai multe ID-uri.
     *
     * @param id identificatorul entității (poate fi unul sau mai multe argumente)
     */
    void sterge(Long... id);

    /**
     * Caută un element în depozit pe baza ID-ului.
     *
     * @param id identificatorul entității căutate
     * @return entitatea găsită
     */
    T cauta(Long... id);

    /**
     * Returnează colecția completă de entități gestionate de depozit.
     *
     * @return o colecție de obiecte de tip {@code T}
     */
    public Collection<T> getDepozit();

    void modifica(T mod);
}
