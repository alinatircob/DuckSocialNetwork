package org.example.ducksocialnetworkm.domeniu.observer;

/**
 * Interfață pentru obiectele observabile (subject).
 * Permite adăugarea, eliminarea și notificarea observatorilor (IObserver).
 */
public interface IObservable {

    /**
     * Abonează un observator la obiectul curent.
     * @param o observatorul care va fi notificat la evenimente
     */
    void subscribe(IObserver o);

    /**
     * Dezabonează un observator din lista curentă.
     * @param o observatorul care nu va mai fi notificat
     */
    void unsubscribe(IObserver o);

    /**
     * Trimite un mesaj tuturor observatorilor abonați.
     * @param mesaj mesajul transmis observatorilor
     */
    void notifySubscribers(String mesaj);
}