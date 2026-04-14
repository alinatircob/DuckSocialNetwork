package org.example.ducksocialnetworkm.domeniu.relatie;

/**
 * Interfață care definește o relație între două entități identificate prin ID-uri.
 */

public interface Relatie {

    /**
     * @return ID-ul primei entități implicate în relație: Long
     */

    public Long getId1();

    /**
     * @return ID-ul celei de-a doua entități implicate în relație: Long
     */

    public Long getId2();
}
