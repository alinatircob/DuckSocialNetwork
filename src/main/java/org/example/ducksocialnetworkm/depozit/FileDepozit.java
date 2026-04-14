package org.example.ducksocialnetworkm.depozit;

import java.io.IOException;

public interface FileDepozit<T> extends Depozit<T> {

    /**
     * Citește datele din fișierul asociat depozitului și le încarcă în memorie.
     * @throws IOException dacă fișierul nu poate fi accesat sau conține erori de citire.
     */

    void citesteFisier() throws IOException;

    /**
     * Scrie datele curente din memorie în fișierul asociat depozitului.
     */

    void scrieFisier();
}
