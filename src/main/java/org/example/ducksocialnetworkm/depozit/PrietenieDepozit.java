package org.example.ducksocialnetworkm.depozit;
import org.example.ducksocialnetworkm.domeniu.relatie.Prietenie;
import org.example.ducksocialnetworkm.domeniu.relatie.Relatie;
import org.example.ducksocialnetworkm.domeniu.validator.ValidationException;

import java.io.*;
import java.util.*;

/**
 * Clasa {@code PrietenieDepozit} gestionează persistenta prieteniilor dintre utilizatori.
 * Aceasta permite adăugarea, ștergerea și citirea prieteniilor dintr-un fișier text.
 */

public class PrietenieDepozit implements FileDepozit<Relatie> {

    private List<Relatie> prietenii;
    private String fisier;

    /**
     * Creează un nou obiect {@code PrietenieDepozit} și încarcă datele din fișierul specificat.
     * @param fisier calea către fișierul de intrare/ieșire.
     */

    public PrietenieDepozit(String fisier) {
        this.fisier = fisier;
        prietenii = new ArrayList<>();
        try{
           citesteFisier();
        } catch(IOException e){
            System.out.println("Fisier invalid!");
            return;
        }
    }

    /**
     * Citește prieteniile din fișierul sursă și le adaugă în lista internă.
     * @throws IOException dacă apare o eroare la citirea fișierului.
     */

    public void citesteFisier() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fisier))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] items = line.split(";");
                adaugaFisier(new Prietenie(Long.parseLong(items[0]), Long.parseLong(items[1])));
            }
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * Scrie toate prieteniile din memorie în fișierul de ieșire.
     */

    public void scrieFisier() {
        try (BufferedWriter wr = new BufferedWriter(new FileWriter(fisier))) {
            for (Relatie p : prietenii) {
                wr.write(p.toString().toUpperCase());
                wr.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Adaugă o prietenie direct în lista internă.
     * @param prietenie obiectul {@link Relatie} care va fi adăugat.
     */

    private void adaugaFisier(Relatie prietenie){
        prietenii.add(prietenie);
    }

    /**
     * Adaugă o prietenie nouă dacă aceasta nu există deja și actualizează fișierul.
     * @param prietenie prietenia de adăugat.
     * @throws ValidationException dacă prietenia există deja.
     */

    public void adauga(Relatie prietenie){
        for (Relatie p : prietenii) {
            if ((Objects.equals(p.getId1(), prietenie.getId1()) && Objects.equals(p.getId2(), prietenie.getId2()) || (Objects.equals(p.getId1(), prietenie.getId2()) && Objects.equals(p.getId2(), prietenie.getId1())))) {
                throw new ValidationException("Exista deja prietenie intre cei 2 useri!\n");
            }
        }
        prietenii.add(prietenie);
        scrieFisier();
    }

    /**
     * Șterge o prietenie existentă dintre doi utilizatori.
     * @param id id-urile prieteniei de șters.
     * @throws ValidationException dacă prietenia nu există.
     */

    public void sterge(Long... id){
        boolean gasit = false;
        Relatie prietenieS = null;
        for (Relatie p : prietenii) {
            if ((Objects.equals(p.getId1(), id[0]) && Objects.equals(p.getId2(), id[1]) || (Objects.equals(p.getId1(), id[1]) && Objects.equals(p.getId2(), id[0])))) {
                prietenieS = prietenii.get(prietenii.indexOf(p));
                gasit = true;
                break;
            }
        }
        if (!gasit) {
            throw new ValidationException("Nu exista prietenie intre cei 2 useri!\n");
        }
        prietenii.remove(prietenieS);
        scrieFisier();
    }

    /**
     * Caută o prietenie după ID-ul specificat.
     *
     * @param id ID-ul prieteniei
     * @return prietenia corespunzătoare ID-ului
     * @throws ValidationException dacă prietenia nu există
     */
    public Relatie cauta(Long... id){
        boolean gasit = false;
        Relatie prietenieS = null;
        for (Relatie p : prietenii) {
            if ((Objects.equals(p.getId1(), id[0]) && Objects.equals(p.getId2(), id[1]) || (Objects.equals(p.getId1(), id[1]) && Objects.equals(p.getId2(), id[0])))) {
                prietenieS = prietenii.get(prietenii.indexOf(p));
                gasit = true;
                break;
            }
        }
        if (!gasit) {
            throw new ValidationException("Nu exista prietenie intre cei 2 useri!\n");
        }
        return prietenieS;
    }

    /**
     * Returnează toate prieteniile curente din memorie.
     * @return o colecție cu obiectele {@link Prietenie}.
     */

    public Collection<Relatie> getDepozit() {
        return prietenii;
    }

    /**
     * Șterge toate prieteniile asociate unui anumit utilizator.
     * @param id identificatorul utilizatorului vizat.
     */

    public void stergePUser(Long id){
        Iterator<Relatie> it = prietenii.iterator();
        while (it.hasNext()) {
            Relatie p = it.next();
            if (p.getId1() == id || p.getId2() == id) {
                it.remove();
            }
        }
        scrieFisier();
    }

    public void modifica(Relatie mod){}

}
