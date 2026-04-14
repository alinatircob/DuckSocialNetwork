package org.example.ducksocialnetworkm.depozit;

import org.example.ducksocialnetworkm.domeniu.card.*;
import org.example.ducksocialnetworkm.domeniu.user.*;
import org.example.ducksocialnetworkm.domeniu.validator.ValidationException;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Clasa {@code CardDepozit} gestionează o colecție de obiecte {@link Card}
 * (de tipuri diferite, precum {@link SwimmingCard} sau {@link FlyingCard}).
 * Informațiile despre carduri sunt stocate într-un fișier text, iar această
 * clasă oferă funcționalități pentru citirea, scrierea, adăugarea, ștergerea
 * și căutarea cardurilor.
 *
 * @param <? extends Rata> tipul raței asociate cardului (de exemplu {@link SwimmingDuck} sau {@link FlyingDuck})
 */

public class CardDepozit implements FileDepozit<Card<? extends Rata>> {

    private Map<Long, Card<? extends Rata>> carduri;
    private String fisier;


    /**
     * Creează un nou depozit de carduri și încarcă datele din fișierul specificat.
     *
     * @param fisier calea către fișierul text din care se citesc cardurile
     */
    public CardDepozit(String fisier) {
        this.fisier = fisier;
        carduri = new HashMap<Long, Card<? extends Rata>>();
        try{
            citesteFisier();
        } catch(IOException e){
            System.out.println("Fisier invalid!");
            return;
        }
    }


    /**
     * Citește informațiile despre carduri din fișierul asociat depozitului.
     * Liniile din fișier trebuie să aibă formatul:
     * <pre>
     * SWIMMINGCARD;id;nume
     * FLYINGCARD;id;nume
     *
     * @throws IOException dacă fișierul nu poate fi citit
     */
    public void citesteFisier() throws IOException{
        try (BufferedReader reader = new BufferedReader(new FileReader(fisier))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] items = line.split(";");
                if(Objects.equals(items[0], "SWIMMINGCARD")){
                    Card<SwimmingDuck> card =  new SwimmingCard(Long.parseLong(items[1]), items[2], "Inotator");
                    adaugaFisier(card);
                }
                else if(Objects.equals(items[0], "FLYINGCARD")){
                    Card<FlyingDuck> card =  new FlyingCard(Long.parseLong(items[1]), items[2], "Zburator");
                    adaugaFisier(card);
                }
            }
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * Adaugă un card în colecția internă fără a rescrie fișierul.
     * Metoda este folosită doar în timpul citirii din fișier.
     *
     * @param card cardul care trebuie adăugat
     */
    private void adaugaFisier(Card<? extends Rata> card){
        carduri.put(card.getId(), card);
    }


    /**
     * Rescrie conținutul fișierului cu toate cardurile curente din depozit.
     * Fiecare linie din fișier va conține informațiile corespunzătoare unui card.
     */
    public void scrieFisier() {
        try (BufferedWriter wr = new BufferedWriter(new FileWriter(fisier))) {
            for (Card<? extends Rata> card : carduri.values()) {
                wr.write(card.toString().toUpperCase());
                wr.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Adaugă un nou card în depozit, dacă nu există deja unul cu același ID.
     * După adăugare, fișierul este actualizat.
     *
     * @param card cardul care trebuie adăugat
     * @throws ValidationException dacă există deja un card cu același ID
     */
    public void adauga(Card<? extends Rata> card){
        if (carduri.containsKey(card.getId())){
            throw new ValidationException("Exista deja un card cu acest ID!\n");
        }
        carduri.put(card.getId(), card);
        scrieFisier();
    }

    /**
     * Șterge cardul cu ID-ul specificat din depozit.
     * După ștergere, fișierul este actualizat.
     *
     * @param id ID-ul cardului de șters
     * @throws ValidationException dacă nu există niciun card cu acest ID
     */
    public void sterge(Long... id){
        if (!carduri.containsKey(id[0])){
            throw new ValidationException("Nu exista un card cu acest ID!\n");
        }
        carduri.remove(id[0]);
        scrieFisier();
    }


    /**
     * Caută și returnează cardul cu ID-ul specificat.
     *
     * @param id ID-ul cardului căutat
     * @return cardul corespunzător ID-ului dat
     * @throws ValidationException dacă nu există niciun card cu acest ID
     */
    public Card<? extends Rata> cauta(Long... id){
        if (!carduri.containsKey(id[0])){
            throw new ValidationException("Nu exista un card cu acest ID!\n");
        }
        return carduri.get(id[0]);
    }


    /**
     * Returnează o colecție care conține toate cardurile din depozit.
     *
     * @return colecția de carduri
     */
    public Collection<Card<? extends Rata>> getDepozit() {
        return carduri.values();
    }

    public void modifica(Card<? extends Rata> mod){}

}
