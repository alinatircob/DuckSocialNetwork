package org.example.ducksocialnetworkm.domeniu.user;

/**
 * Clasă abstractă care implementează modelul Decorator pentru obiecte de tip {@link Rata}.
 * Permite extinderea comportamentului unei rațe fără a modifica clasa de bază.
 */

public abstract class RataDecorator extends Rata {

    /** Referință la obiectul {@link Rata} decorat. */
    protected Rata rata;

    /**
     * Creează un decorator care extinde o rață existentă.
     * @param rata rața decorată
     */
    public RataDecorator(Rata rata) {
        super(rata.getId(), rata.getUsername(), rata.getEmail(), rata.getPassword(), rata.getRezistenta(), rata.getViteza(), rata.getTip());
        this.rata = rata;
    }

    /**
     * Execută acțiunea raței decorate.
     * Poate fi extinsă în clasele derivate pentru a adăuga comportamente suplimentare.
     */
    @Override
    public void actiune() {
        rata.actiune();
    }

    /**
     * Returnează rața decorată.
     * @return instanța {@link Rata} decorată
     */
    public Rata getDecorata() {
        return rata;
    }
}
