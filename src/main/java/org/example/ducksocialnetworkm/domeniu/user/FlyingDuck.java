package org.example.ducksocialnetworkm.domeniu.user;

/**
 * Clasă care reprezintă o rață zburătoare.
 * Extinde funcționalitatea unei {@link Rata} prin intermediul decoratorului {@link RataDecorator}
 * și implementează comportamentul definit de interfața {@link Zburator}.
 */

public class FlyingDuck extends RataDecorator implements Zburator {

    /**
     * Creează o instanță de rață zburătoare bazată pe o rață existentă.
     * @param rata rața de bază decorată
     */
    public FlyingDuck(Rata rata) {
        super(rata);
    }

    /**
     * Afișează acțiunea specifică unei rațe zburătoare.
     */
    @Override
    public void zboara() {
        System.out.println("Pot sa zbor!");
    }

    /**
     * Execută acțiunea generală a raței și apoi acțiunea de zbor.
     */
    @Override
    public void actiune() {
        super.actiune();
        zboara();
    }
}
