package org.example.ducksocialnetworkm.domeniu.event;
import org.example.ducksocialnetworkm.domeniu.observer.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Clasa abstractă {@code Event} reprezintă un eveniment din sistem (ex. o cursă de rațe).
 * Evenimentul implementează interfața {@link IObservable}, permițând atașarea de observatori.
 */

public abstract class Event implements IObservable {

    protected Long id;
    protected String nume;
    protected Long idCreator;
    protected List<IObserver> observers;
    protected String status = "OPEN";
    protected String rezultatFinal = "";

    /**
     * Creează un eveniment cu un ID și un nume specific.
     * @param id    identificatorul unic al evenimentului
     * @param nume  numele evenimentului
     */
    public Event(Long id, String nume,Long idCreator) {
        this.id = id;
        this.nume = nume;
        this.idCreator = idCreator;
        this.observers = new ArrayList<>();
    }

    /** {@inheritDoc} */
    @Override
    public void subscribe(IObserver s) { observers.add(s); }

    /** {@inheritDoc} */
    @Override
        public void unsubscribe(IObserver s) { observers.remove(s); }

    /** {@inheritDoc} */
    @Override
    public void notifySubscribers(String mesaj) {
        observers.forEach(s -> s.update(mesaj));
    }

    /** @return ID-ul evenimentului */
    public Long getId() { return id; }

    /** @return numele evenimentului */
    public String getNume() { return nume; }

    /** @return lista observatorilor atașați evenimentului */
    public List<IObserver> getObservers() {
        return observers;
    }

    public Long getIdCreator() { return idCreator; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public String getRezultatFinal() { return rezultatFinal; }

    public void setRezultatFinal(String rez) { this.rezultatFinal = rez; }

    /**
     * Metodă abstractă care pornește evenimentul concret.
     * Implementarea specifică este definită în subclase (ex. {@link RaceEvent}).
     */
    public abstract void start();
}