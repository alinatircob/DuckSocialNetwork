package org.example.ducksocialnetworkm.domeniu.validator;

/**
 * Interfață generică pentru validarea obiectelor din domeniu.
 * @param <T> tipul obiectului care urmează să fie validat
 */

public interface ValidatorDomeniu<T> {

    /**
     * Validează un obiect din domeniu și aruncă o excepție dacă datele nu sunt valide.
     * @param var obiectul de validat
     * @throws ValidationException dacă obiectul conține date invalide
     */

    void valideaza(T var) throws ValidationException;
}
