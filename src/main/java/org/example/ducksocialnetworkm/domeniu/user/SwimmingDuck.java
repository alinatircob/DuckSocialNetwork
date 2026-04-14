package org.example.ducksocialnetworkm.domeniu.user;

/**
 * Clasă care reprezintă o rață înotătoare.
 * Extinde funcționalitatea unei {@link Rata} prin intermediul {@link RataDecorator}
 * și implementează comportamentul definit de interfața {@link Inotator}.
 */
public class SwimmingDuck extends RataDecorator implements Inotator {

    /**
     * Creează o instanță de rață înotătoare bazată pe o rață existentă.
     * @param rata rața de bază care este decorată
     */
    public SwimmingDuck(Rata rata) {
        super(rata);
    }

    /**
     * Afișează acțiunea specifică unei rațe înotătoare.
     */
    @Override
    public void inoata() {
        System.out.println("Pot sa inot!");
    }

    /**
     * Execută acțiunea raței decorate și apoi adaugă comportamentul de înot.
     */
    @Override
    public void actiune() {
        super.actiune();
        inoata();
    }

}