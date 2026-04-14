package org.example.ducksocialnetworkm.depozit;

import org.example.ducksocialnetworkm.domeniu.event.*;
import org.example.ducksocialnetworkm.domeniu.observer.IObserver;
import org.example.ducksocialnetworkm.domeniu.user.*;
import org.example.ducksocialnetworkm.domeniu.user.Rata;
import org.example.ducksocialnetworkm.domeniu.validator.ValidationException;
import org.example.ducksocialnetworkm.factory.*;

import java.io.*;
import java.util.*;

/**
 * Clasa {@code EventDepozit} gestionează evenimentele din cadrul aplicației.
 * Aceasta permite adăugarea, ștergerea și căutarea evenimentelor, oferind o
 * mapare internă între ID-ul fiecărui eveniment și obiectul corespunzător.
 */

public class EventDepozit implements FileDepozit<Event> {

    private Map<Long, Event> events;
    private String fisier;

    /**
     * Creează un nou obiect {@code EventDepozit} cu o mapă goală de evenimente.
     */
    public EventDepozit(String fisier) {
        this.fisier = fisier;
        events = new HashMap<Long, Event>();
        try{
            citesteFisier();
        } catch(IOException e){
            System.out.println("Fisier invalid!");
            return;
        }
    }

    public void scrieFisier() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fisier))) {
            for (Event e : events.values()) {
                StringBuilder sb = new StringBuilder();
                sb.append(e.getId()).append("/").append(e.getNume()).append("/");

                // observeri -> toti Userii la fel ca in UserDepozit
                List<IObserver> obs = e.getObservers();
                for (int i = 0; i < obs.size(); i++) {
                    if (i > 0) sb.append("|"); // separator intre useri
                    if (obs.get(i) instanceof Persoana) {
                        sb.append(((Persoana) obs.get(i)).toString().toUpperCase());
                    }
                    if (obs.get(i) instanceof Rata) {
                        sb.append(((Rata) obs.get(i)).toString().toUpperCase());
                    }
                }
                sb.append("/");

                // distante (doar pentru RaceEvent)
                if (e instanceof RaceEvent) {
                    List<Double> distante = ((RaceEvent) e).getDistante();
                    for (int i = 0; i < distante.size(); i++) {
                        if (i > 0) sb.append(",");
                        sb.append(distante.get(i));
                    }
                }

                bw.write(sb.toString());
                bw.newLine();
            }
        } catch (IOException ex) {
            throw new RuntimeException("Eroare la scrierea în fișier: " + ex.getMessage(), ex);
        }
    }

    public void citesteFisier() throws IOException {
        File f = new File(fisier);
        if (!f.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linie;
            while ((linie = br.readLine()) != null) {
                String[] parts = linie.split("/", -1);
                if (parts.length >= 4) {
                    Long id = Long.parseLong(parts[0]);
                    String nume = parts[1];

                    // observeri
                    List<User> observers = new ArrayList<>();
                    if (!parts[2].isEmpty()) {
                        String[] observerStrings = parts[2].split("\\|");
                        for (String s : observerStrings) {
                            String[] items = s.split(";");
                            if (items[0].equalsIgnoreCase("RATA")) {
                                Rata r = DuckFactory.creeazaRata(
                                        Long.parseLong(items[1]), items[2], items[3], items[4],
                                        Double.parseDouble(items[5]), Double.parseDouble(items[6]), items[7]
                                );
                                r.setIdCard(Long.parseLong(items[8]));
                                observers.add(r);
                            } else if (items[0].equalsIgnoreCase("PERSOANA")) {
                                Persoana p = new Persoana(
                                        Long.parseLong(items[1]), items[2], items[3], items[4],
                                        items[5], items[6], items[7], items[8]
                                );
                                observers.add(p);
                            }
                        }
                    }

                    // distante
                    List<Double> distante = new ArrayList<>();
                    if (!parts[3].isEmpty()) {
                        for (String s : parts[3].split(",")) {
                            distante.add(Double.parseDouble(s));
                        }
                    }

                    // cream RaceEvent
                    RaceEvent e = new RaceEvent(id, nume, distante,null);
                    for (User u : observers) e.subscribe(u);

                    events.put(id, e);
                }
            }
        } catch (IOException ex) {
            throw ex;
        }
    }

    /**
     * Adaugă un eveniment nou în depozit, verificând dacă ID-ul acestuia este unic.
     *
     * @param event evenimentul de adăugat
     * @throws ValidationException dacă există deja un eveniment cu același ID
     */
    public void adauga(Event event){
        if (events.containsKey(event.getId())){
            throw new ValidationException("Exista deja un eveniment cu acest ID!\n");
        }
        events.put(event.getId(), event);
        scrieFisier();
    }

    /**
     * Șterge un eveniment pe baza ID-ului său și actualizează fișierul.
     *
     * @param id ID-ul evenimentului de șters
     * @throws ValidationException dacă evenimentul nu există
     */
    public void sterge(Long... id){
        if (!events.containsKey(id[0])){
            throw new ValidationException("Nu exista un eveniment cu acest ID!\n");
        }
        events.remove(id[0]);
        scrieFisier();
    }

    /**
     * Caută un eveniment după ID-ul specificat.
     *
     * @param id ID-ul evenimentului de căutat
     * @return obiectul {@link Event} corespunzător
     * @throws ValidationException dacă evenimentul nu există
     */
    public Event cauta(Long... id){
        if (!events.containsKey(id[0])){
            throw new ValidationException("Nu exista un eveniment cu acest ID!\n");
        }
        return events.get(id[0]);
    }

    /**
     * Returnează toate evenimentele existente în memorie.
     *
     * @return o colecție de obiecte {@link Event}
     */
    public Collection<Event> getDepozit() {
        return events.values();
    }

    public void modifica(Event mod){
        scrieFisier();
    }

}
