package org.example.ducksocialnetworkm.interfata;

import org.example.ducksocialnetworkm.serviciu.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static java.lang.System.exit;

/**
 * Clasa care implementează interfața aplicației, permițând interacțiunea
 * cu utilizatorul prin intermediul consolei.
 */

public class Interfata implements IInterfata {

    private IServiciu serviciu;
    private Scanner sc = new Scanner(System.in);

    /**
     * Constructor pentru inițializarea interfeței.
     * @param serviciu instanța serviciului care gestionează logica aplicației
     */

    public Interfata(Serviciu serviciu) {
        this.serviciu = serviciu;
    }

    /**
     * Metoda principală care rulează aplicația.
     *
     * <p>Prezintă un meniu cu opțiuni, citește comenzile utilizatorului
     * și apelează metodele corespunzătoare din {@link Serviciu}.</p>
     * <p>Toate erorile de introducere (ex. valori numerice invalide)
     * sunt gestionate și afișate prin mesaje.</p>
     */

    public void ruleaza() {
        while(true) {
            System.out.println("OPTIUNI:");
            System.out.println("1. Adauga user");
            System.out.println("2. Sterge user");
            System.out.println("3. Adauga prietenie");
            System.out.println("4. Sterge prietenie");
            System.out.println("5. Afiseaza numar de comunitati");
            System.out.println("6. Afiseaza cea mai sociabila comunitate");
            System.out.println("7. Adauga card");
            System.out.println("8. Sterge card");
            System.out.println("9. Adauga rata in card");
            System.out.println("10. Sterge rata din card");
            System.out.println("11. Afiseaza performanta medie card");
            System.out.println("12. Adauga eveniment");
            System.out.println("13. Sterge eveniment");
            System.out.println("14. Adauga observer la eveniment");
            System.out.println("15. Sterge observer din eveniment");
            System.out.println("16. Porneste evenimentul");
            System.out.println("17. Exit");

            int cmd = 0;
            try {
                cmd = Integer.parseInt(sc.nextLine().trim().toUpperCase());
            } catch (NumberFormatException e) {
                System.out.println("Optiune invalida");
            }
            switch (cmd){
                case 1:
                    System.out.println("Tip:");
                    String tip = sc.nextLine().trim().toUpperCase();
                    if(tip.equals("RATA")){
                        System.out.println("ID:");
                        Long id;
                        try{
                            id = Long.parseLong(sc.nextLine().trim().toUpperCase());
                        }catch(NumberFormatException e){
                            System.out.println("ID invalid!");
                            break;
                        }
                        System.out.println("Username:");
                        String username = sc.nextLine().trim().toUpperCase();
                        System.out.println("Email:");
                        String email = sc.nextLine().trim().toUpperCase();
                        System.out.println("Password:");
                        String password = sc.nextLine().trim().toUpperCase();
                        System.out.println("Viteza:");
                        Double viteza;
                        try{
                            viteza = Double.parseDouble(sc.nextLine().trim().toUpperCase());
                        }catch(NumberFormatException e){
                            System.out.println("Viteza invalida!");
                            break;
                        }
                        System.out.println("Rezistenta:");
                        Double rezistenta;
                        try{
                            rezistenta = Double.parseDouble(sc.nextLine().trim().toUpperCase());
                        }catch(NumberFormatException e){
                            System.out.println("Rezistenta invalida!");
                            break;
                        }
                        System.out.println("Tip(FLYING, SWIMMING sau FLYING_AND_SWIMMING):");
                        String tipR = sc.nextLine().trim().toUpperCase();
                        System.out.println(serviciu.adaugaRata(id, username, email, password, rezistenta, viteza, tipR));
                    }
                    else if(tip.equals("PERSOANA")){
                        System.out.println("ID:");
                        Long id;
                        try{
                            id = Long.parseLong(sc.nextLine().trim().toUpperCase());
                        }catch(NumberFormatException e){
                            System.out.println("ID invalid!");
                            break;
                        }
                        System.out.println("Username:");
                        String username = sc.nextLine().trim().toUpperCase();
                        System.out.println("Email:");
                        String email = sc.nextLine().trim().toUpperCase();
                        System.out.println("Password:");
                        String password = sc.nextLine().trim().toUpperCase();
                        System.out.println("Nume:");
                        String nume = sc.nextLine().trim().toUpperCase();
                        System.out.println("Prenume:");
                        String prenume = sc.nextLine().trim().toUpperCase();
                        System.out.println("Data nasterii:");
                        String dataNasterii = sc.nextLine().trim().toUpperCase();
                        System.out.println("Ocupatie:");
                        String ocupatie = sc.nextLine().trim().toUpperCase();
                        //System.out.println("hvcj");
                        System.out.println(serviciu.adaugaPersoana(id, username, email, password, nume, prenume, dataNasterii, ocupatie));
                    }
                    else{
                        System.out.println("Tip invalid!");
                        break;
                    }
                    break;
                case 2:
                    System.out.println("ID:");
                    Long id;
                    try{
                        id = Long.parseLong(sc.nextLine().trim().toUpperCase());
                    }catch(NumberFormatException e){
                        System.out.println("ID invalid!");
                        break;
                    }
                    System.out.println(serviciu.stergeUser(id));
                    break;
                case 3:
                    System.out.println("ID primul user:");
                    Long id1;
                    try{
                        id1 = Long.parseLong(sc.nextLine().trim().toUpperCase());
                    }catch(NumberFormatException e){
                        System.out.println("ID invalid!");
                        break;
                    }
                    System.out.println("ID al doilea user:");
                    Long id2;
                    try{
                        id2 = Long.parseLong(sc.nextLine().trim().toUpperCase());
                    }catch(NumberFormatException e){
                        System.out.println("ID invalid!");
                        break;
                    }
                    System.out.println(serviciu.adaugaPrietenie(id1, id2));
                    break;
                case 4:
                    System.out.println("ID primul user:");
                    try{
                        id1 = Long.parseLong(sc.nextLine().trim().toUpperCase());
                    }catch(NumberFormatException e){
                        System.out.println("ID invalid!");
                        break;
                    }
                    System.out.println("ID al doilea user:");
                    try{
                        id2 = Long.parseLong(sc.nextLine().trim().toUpperCase());
                    }catch(NumberFormatException e){
                        System.out.println("ID invalid!");
                        break;
                    }
                    System.out.println(serviciu.stergePrietenie(id1, id2));
                    break;
                case 5:
                    System.out.println(serviciu.numarComunitati());
                    break;
                case 6:
                    System.out.println(serviciu.ceaMaiSociabilaComunitate());
                    break;
                case 7:
                    System.out.println("ID:");
                    try{
                        id = Long.parseLong(sc.nextLine().trim().toUpperCase());
                    }catch(NumberFormatException e){
                        System.out.println("ID invalid!");
                        break;
                    }
                    System.out.println("Nume:");
                    String nume = sc.nextLine().trim().toUpperCase();
                    System.out.println("Tip:");
                    tip = sc.nextLine().trim().toUpperCase();
                    System.out.println(serviciu.adaugaCard(tip, id, nume));
                    break;
                case 8:
                    System.out.println("ID:");
                    try{
                        id = Long.parseLong(sc.nextLine().trim().toUpperCase());
                    }catch(NumberFormatException e){
                        System.out.println("ID invalid!");
                        break;
                    }
                    System.out.println(serviciu.stergeCard(id));
                    break;
                case 9:
                    Long idC;
                    System.out.println("ID rata:");
                    try{
                        id = Long.parseLong(sc.nextLine().trim().toUpperCase());
                    }catch(NumberFormatException e){
                        System.out.println("ID invalid!");
                        break;
                    }
                    System.out.println("ID card:");
                    try{
                        idC = Long.parseLong(sc.nextLine().trim().toUpperCase());
                    }catch(NumberFormatException e){
                        System.out.println("ID invalid!");
                        break;
                    }
                    System.out.println(serviciu.adaugaRataCard(id, idC));
                    break;
                case 10:
                    System.out.println("ID rata:");
                    try{
                        id = Long.parseLong(sc.nextLine().trim().toUpperCase());
                    }catch(NumberFormatException e){
                        System.out.println("ID invalid!");
                        break;
                    }
                    System.out.println("ID card:");
                    try{
                        idC = Long.parseLong(sc.nextLine().trim().toUpperCase());
                    }catch(NumberFormatException e){
                        System.out.println("ID invalid!");
                        break;
                    }
                    System.out.println(serviciu.stergeRataCard(id, idC));
                    break;
                case 11:
                    System.out.println("ID:");
                    try{
                        id = Long.parseLong(sc.nextLine().trim().toUpperCase());
                    }catch(NumberFormatException e){
                        System.out.println("ID invalid!");
                        break;
                    }
                    System.out.println(serviciu.performantaMedieCard(id));
                    break;
                case 12:
                    Long idCreator;
                    System.out.println("ID:");
                    try {
                        id = Long.parseLong(sc.nextLine().trim().toUpperCase());
                    } catch (NumberFormatException e) {
                        System.out.println("ID invalid!");
                        break;
                    }
                    System.out.println("ID Creator:");
                    try {
                        idCreator = Long.parseLong(sc.nextLine().trim().toUpperCase());
                    } catch (NumberFormatException e) {
                        System.out.println("ID invalid!");
                        break;
                    }
                    System.out.println("Nume:");
                    nume = sc.nextLine().trim().toUpperCase();
                    System.out.println("Numarul de culoare:");
                    int n;
                    try {
                        n = Integer.parseInt(sc.nextLine().trim().toUpperCase());
                    } catch (NumberFormatException e) {
                        System.out.println("Numar invalid!");
                        break;
                    }
                    List<Double> distante = new ArrayList<>();
                    try {
                        for (int i=0; i<n; i++) {
                            System.out.println("Baliza pe culoarul: " + (i+1));
                            Double d = Double.parseDouble(sc.nextLine().trim().toUpperCase());
                            distante.add(d);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Distante invalide!");
                        break;
                    }
                    System.out.println(serviciu.adaugaEvent(id, nume, distante, idCreator));
                    break;
                case 13:
                    System.out.println("ID:");
                    try {
                        id = Long.parseLong(sc.nextLine().trim().toUpperCase());
                    } catch (NumberFormatException e) {
                        System.out.println("ID invalid!");
                        break;
                    }
                    System.out.println(serviciu.stergeEvent(id));
                    break;
                case 14:
                    System.out.println("ID user (observer):");
                    try {
                        id = Long.parseLong(sc.nextLine().trim().toUpperCase());
                    } catch (NumberFormatException e) {
                        System.out.println("ID invalid!");
                        break;
                    }
                    Long idE;
                    System.out.println("ID eveniment:");
                    try {
                        idE = Long.parseLong(sc.nextLine().trim().toUpperCase());
                    } catch (NumberFormatException e) {
                        System.out.println("ID invalid!");
                        break;
                    }
                    System.out.println(serviciu.adaugaObserver(id, idE));
                    break;
                case 15:
                    System.out.println("ID user (observer):");
                    try {
                        id = Long.parseLong(sc.nextLine().trim().toUpperCase());
                    } catch (NumberFormatException e) {
                        System.out.println("ID invalid!");
                        break;
                    }
                    System.out.println("ID eveniment:");
                    try {
                        idE = Long.parseLong(sc.nextLine().trim().toUpperCase());
                    } catch (NumberFormatException e) {
                        System.out.println("ID invalid!");
                        break;
                    }

                    System.out.println(serviciu.stergeObserver(id, idE));
                    break;
                case 16:
                    System.out.println("ID:");
                    try {
                        idE = Long.parseLong(sc.nextLine().trim().toUpperCase());
                    } catch (NumberFormatException e) {
                        System.out.println("ID invalid!");
                        break;
                    }
                    //System.out.println(serviciu.startEvent(idE));
                    break;
                case 17:
                    exit(0);
                default:
                    System.out.println("Optiune invalida!");
                    break;
            }

        }
    }

}
