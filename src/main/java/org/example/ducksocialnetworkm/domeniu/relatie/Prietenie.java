package org.example.ducksocialnetworkm.domeniu.relatie;

/**
 * Clasă care modelează o relație de prietenie între doi utilizatori.
 * Implementarea interfeței {@link Relatie}.
 */

public class Prietenie implements Relatie {

    private Long id1;
    private Long id2;

    /**
     * Constructor pentru crearea unei prietenii între doi utilizatori.
     * @param id1 ID-ul primului utilizator: Long
     * @param id2 ID-ul celui de-al doilea utilizator: Long
     */

    public Prietenie(Long id1, Long id2) {
        this.id1 = id1;
        this.id2 = id2;
    }

    /** {@inheritDoc} */

    public Long getId1() {
        return id1;
    }

    /** {@inheritDoc} */

    public Long getId2() {
        return id2;
    }

    /**
     * @return o reprezentare textuală a prieteniei sub forma "id1;id2"
     */

    @Override
    public String toString() {
        return id1 + ";" + id2;
    }
}
