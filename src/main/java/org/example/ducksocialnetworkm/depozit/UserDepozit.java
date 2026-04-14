package org.example.ducksocialnetworkm.depozit;

import org.example.ducksocialnetworkm.domeniu.user.Persoana;
import org.example.ducksocialnetworkm.domeniu.user.Rata;
import org.example.ducksocialnetworkm.domeniu.user.User;
import org.example.ducksocialnetworkm.domeniu.validator.ValidationException;
import org.example.ducksocialnetworkm.factory.DuckFactory;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Clasa {@code UserDepozit} gestionează persistenta utilizatorilor în cadrul aplicației.
 * Aceasta permite adăugarea, ștergerea și gestionarea relațiilor de prietenie între utilizatori.
 */

public class UserDepozit implements FileDepozit<User> {

    private Map<Long, User> useri;
    private String fisier;

    /**
     * Creează un nou obiect {@code UserDepozit} și încarcă datele din fișierul specificat.
     * @param fisier calea către fișierul de intrare/ieșire.
     */

    public UserDepozit(String fisier) {
        this.fisier = fisier;
        useri = new HashMap<Long, User>();
        try{
            citesteFisier();
        } catch(IOException e){
            System.out.println("Fisier invalid!");
            return;
        }
    }


    /**
     * Citește utilizatorii din fișier și îi adaugă în mapa internă.
     * @throws IOException dacă apare o eroare de citire.
     */

    public void citesteFisier() throws IOException{
        try (BufferedReader reader = new BufferedReader(new FileReader(fisier))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] items = line.split(";");
                if(Objects.equals(items[0], "RATA")){
                    Rata rata = DuckFactory.creeazaRata(Long.parseLong(items[1]), items[2], items[3], items[4], Double.parseDouble(items[5]), Double.parseDouble(items[6]), items[7]);
                    rata.setIdCard(Long.parseLong(items[8]));
                    adaugaFisier(rata);
                }
                else if(Objects.equals(items[0], "PERSOANA")){
                    Persoana persoana = new Persoana(Long.parseLong(items[1]), items[2], items[3], items[4], items[5], items[6], items[7], items[8]);
                    adaugaFisier(persoana);
                }
            }
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * Scrie toți utilizatorii din memorie în fișierul de ieșire.
     */

    public void scrieFisier() {
        try (BufferedWriter wr = new BufferedWriter(new FileWriter(fisier))) {
            for (User u : useri.values()) {
                wr.write(u.toString().toUpperCase());
                wr.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Adaugă un utilizator în mapa internă fără validare suplimentară.
     * @param user utilizatorul de adăugat.
     */

    private void adaugaFisier(User user){
        useri.put(user.getId(), user);
    }


    /**
     * Adaugă un utilizator nou, verificând unicitatea ID-ului.
     * @param user utilizatorul de adăugat.
     * @throws ValidationException dacă ID-ul există deja.
     */

    public void adauga(User user){
        if (useri.containsKey(user.getId())){
            throw new ValidationException("Exista deja un user cu acest ID!\n");
        }
        useri.put(user.getId(), user);
        scrieFisier();
    }

    /**
     * Șterge un utilizator și elimină referințele sale din listele de prieteni ale celorlalți.
     * @param id utilizatorul de șters.
     * @throws ValidationException dacă utilizatorul nu există.
     */

    public void sterge(Long... id){
        if (!useri.containsKey(id[0])){
            throw new ValidationException("Nu exista un user cu acest ID!\n");
        }
        for(User u : useri.get(id[0]).getPrieteni()){
            u.stergePrieten(useri.get(id[0]));
        }
        useri.remove(id[0]);
        scrieFisier();
    }

    /**
     * Caută un utilizator după ID-ul specificat.
     *
     * @param id ID-ul utilizatorului
     * @return utilizatorul găsit
     * @throws ValidationException dacă utilizatorul nu există
     */
    public User cauta(Long... id){
        if (!useri.containsKey(id[0])){
            throw new ValidationException("Nu exista un user cu acest ID!\n");
        }
        return useri.get(id[0]);
    }


    /**
     * Returnează o colecție cu toți utilizatorii din memorie.
     * @return colecția de utilizatori.
     */

    public Collection<User> getDepozit() {
        return useri.values();
    }

    public void modifica(User mod){
        scrieFisier();
    }

}
