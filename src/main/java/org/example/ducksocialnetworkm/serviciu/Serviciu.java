package org.example.ducksocialnetworkm.serviciu;

import org.example.ducksocialnetworkm.depozit.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.example.ducksocialnetworkm.depozit.EventDepozitDB;
import org.example.ducksocialnetworkm.depozit.paging.Page;
import org.example.ducksocialnetworkm.depozit.paging.Pageable;
import org.example.ducksocialnetworkm.depozit.paging.PagingRelatieRepository;
import org.example.ducksocialnetworkm.domeniu.card.Card;
import org.example.ducksocialnetworkm.domeniu.card.FlyingCard;
import org.example.ducksocialnetworkm.domeniu.card.SwimmingCard;
import org.example.ducksocialnetworkm.domeniu.event.Event;
import org.example.ducksocialnetworkm.domeniu.event.RaceEvent;
import org.example.ducksocialnetworkm.domeniu.mesaj.Mesaj;
import org.example.ducksocialnetworkm.domeniu.observer.IObserver;
import org.example.ducksocialnetworkm.domeniu.observer.Observer;
import org.example.ducksocialnetworkm.domeniu.observer.Observable;
import org.example.ducksocialnetworkm.domeniu.relatie.CererePrietenie;
import org.example.ducksocialnetworkm.domeniu.relatie.Prietenie;
import org.example.ducksocialnetworkm.domeniu.relatie.Relatie;
import org.example.ducksocialnetworkm.domeniu.user.*;
import org.example.ducksocialnetworkm.domeniu.validator.*;
import org.example.ducksocialnetworkm.factory.DuckFactory;
import org.example.ducksocialnetworkm.utils.events.ChangeEventType;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Clasa {@code Serviciu} reprezintă stratul de logică al aplicației.
 * Aceasta gestionează interacțiunea dintre depozite și entitățile din domeniu,
 * oferind operații pentru adăugarea, ștergerea și gestionarea utilizatorilor, rațelor și prieteniilor.
 */

public class Serviciu implements IServiciu, Observable<ChangeEventType> {

    private final RataDepozit userDepozit;
    private final PagingRelatieRepository prietenieDepozit;
    private ValidatorDomeniu validator;
    private final Depozit<Card<? extends Rata>> cardDepozit;
    private final IEventDepozit eventDepozit;
    private final List<Observer<ChangeEventType>> observers = new ArrayList<>();
    private MesajDepozit messageDepozit;
    private CerereDepozit cerereDepozit;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    /**
     * Constructorul clasei {@code Serviciu}.
     * Inițializează serviciul cu depozitele primite și sincronizează prieteniile dintre utilizatori.
     * @param userDepozit depozitul de utilizatori
     * @param prietenieDepozit depozitul de prietenii
     */

    public Serviciu(RataDepozit userDepozit, PagingRelatieRepository prietenieDepozit, Depozit<Card<? extends Rata>> cardDepozit, IEventDepozit eventDepozit) {
        this.userDepozit = userDepozit;
        this.prietenieDepozit = prietenieDepozit;
        //initPrietenii();
        this.cardDepozit = cardDepozit;
        //initCarduri();
        this.eventDepozit = eventDepozit;
    }

    @Override
    public void addObserver(Observer<ChangeEventType> observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer<ChangeEventType> observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(ChangeEventType event) {
        for (Observer<ChangeEventType> obs : observers) {
            obs.update(event);
        }
    }

    public void setCerereDepozit(CerereDepozitDB cerereDepozit) {
        this.cerereDepozit = cerereDepozit;
    }

    /**
     * Returnează lista de cereri primite (status PENDING).
     */
    public List<CererePrietenie> getCereriPrimite(Long idUser) {
        return cerereDepozit.findPendingRequests(idUser);
    }

    public String trimiteCererePrietenie(Long idExpeditor, Long idDestinatar) {
        if (idExpeditor.equals(idDestinatar)) return "Nu îți poți trimite cerere singur!";
        try {
            prietenieDepozit.cauta(idExpeditor, idDestinatar);
            return "Sunteți deja prieteni!";
        } catch (ValidationException e) {
            // Nu sunt prieteni in tabelul 'prietenii'
        }

        if (cerereDepozit.existaCerereActiva(idExpeditor, idDestinatar)) {
            return "Există deja o cerere activă sau sunteți deja prieteni!";
        }

        Long newId = System.currentTimeMillis();

        try {
            CererePrietenie cerere = new CererePrietenie(newId, idExpeditor, idDestinatar, "PENDING", LocalDateTime.now());
            cerereDepozit.adauga(cerere);
            notifyObservers(ChangeEventType.FRIENDSHIP);
            return "Cerere trimisă cu succes!";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public void raspundeCerere(Long idExpeditor, Long idDestinatar, String raspuns) {
        CererePrietenie cerere = cerereDepozit.findPendingBetween(idExpeditor, idDestinatar);

        if (cerere != null) {
            cerere.setStatus(raspuns);
            cerereDepozit.modifica(cerere);
            if ("APPROVED".equals(raspuns)) {
                adaugaPrietenie(idExpeditor, idDestinatar);
            }
            notifyObservers(ChangeEventType.FRIENDSHIP);
        } else {
            throw new ValidationException("Nu există o cerere în așteptare între acești utilizatori!");
        }
    }

    public void setMessageDepozit(MesajDepozit messageDepozit) {
        this.messageDepozit = messageDepozit;
    }


    public User login(String usernameOrEmail, String rawPassword) {
        String encryptedInput = org.example.ducksocialnetworkm.utils.password.Password.encrypt(rawPassword);

        return userDepozit.getDepozit().stream()
                .filter(u -> (u.getUsername().equals(usernameOrEmail) || u.getEmail().equals(usernameOrEmail))
                        && u.getPassword().equals(encryptedInput))
                .findFirst()
                .orElse(null);
    }


    public String getDecryptedPassword(Long userId) {
        User u = userDepozit.cauta(userId);
        if (u != null) {
            return org.example.ducksocialnetworkm.utils.password.Password.decrypt(u.getPassword());
        }
        return null;
    }

    public Map<Long, Integer> getUnreadCounts(Long userId) {
        return messageDepozit.getUnreadCounts(userId);
    }

    public void markConversationAsRead(Long myId, Long senderId) {
        messageDepozit.markAsRead(myId, senderId);
        notifyObservers(ChangeEventType.MESSAGE);
    }


    public void trimiteMesaj(Long fromId, Long toId, String text, Mesaj replyTo) {
        CompletableFuture.runAsync(() -> {
                    try {
                        User from = userDepozit.cauta(fromId);
                        User to = userDepozit.cauta(toId);
                        List<User> toList = new ArrayList<>();
                        toList.add(to);

                        Mesaj m;
                        if (replyTo != null) {
                            m = new org.example.ducksocialnetworkm.domeniu.mesaj.ReplyMesaj(from, toList, text, replyTo);
                        } else {
                            m = new Mesaj(from, toList, text);
                        }

                        messageDepozit.save(m);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, executorService)
                .thenRun(() -> {
                    notifyObservers(ChangeEventType.MESSAGE);
                });
    }

    public List<Mesaj> getConversatie(Long id1, Long id2) {
        return messageDepozit.getConversation(id1, id2);
    }

//    public void initCarduri(){
//        for(User u : userDepozit.getDepozit()){
//            if(u instanceof Rata){
//                Rata r = (Rata) u;
//                if(r.getIdCard() != -1){
//                    Card<? extends Rata> card=cardDepozit.cauta(r.getIdCard());
//                    if (card.getTip().equals("Zburator")) {
//                        FlyingDuck fd = getDecorataCompatibila(r, FlyingDuck.class);
//                        if (fd != null) {
//                            ((FlyingCard) card).adaugaRata(fd);
//                        }
//                    } else if (card.getTip().equals("Inotator")) {
//                        SwimmingDuck sd = getDecorataCompatibila(r, SwimmingDuck.class);
//                        if (sd != null) {
//                            ((SwimmingCard) card).adaugaRata(sd);
//                        }
//                    }
//                }
//            }
//        }
//    }

    private void initPrietenii(){
        for(Relatie p : prietenieDepozit.getDepozit()){
            User user1=userDepozit.cauta(p.getId1());
            User user2=userDepozit.cauta(p.getId2());
            user1.adaugaPrieten(user2);
            user2.adaugaPrieten(user1);
        }
    }

    /**
     * Returnează toate evenimentele (pentru popularea tabelului).
     */
    public Collection<Event> getEvents() {
        return eventDepozit.getDepozit();
    }

    /**
     * Adaugă un eveniment nou. DOAR O PERSOANĂ POATE CREA EVENIMENTE.
     * Lista de participanți este inițial goală.
     *
     * @param id ID eveniment
     * @param nume Nume eveniment
     * @param distante Distanțe
     * @param idCreator ID-ul userului care a inițiat crearea
     */
    public String adaugaEvent(Long id, String nume, List<Double> distante, Long idCreator) {
        User creator = userDepozit.cauta(idCreator);
        if (!(creator instanceof Persoana)) {
            return "Doar utilizatorii de tip PERSOANĂ pot crea evenimente!";
        }

        validator = new EventValidator();
        try {
            Event event = new RaceEvent(id, nume, distante, idCreator);
            validator.valideaza(event);

            eventDepozit.adauga(event);

            notifyObservers(ChangeEventType.EVENT);
        } catch (ValidationException e) {
            return e.getMessage();
        }
        return "Eveniment creat cu succes!";
    }

    /**
     * Permite unei Rațe să se înscrie la un eveniment OPEN.
     */
    public String inscrieParticipant(Long idEvent, Long idRata) {
        try {
            Event event = eventDepozit.cauta(idEvent);
            User user = userDepozit.cauta(idRata);
            if (!"OPEN".equals(event.getStatus())) {
                return "Înscrierile sunt închise pentru acest eveniment!";
            }
            if (!(user instanceof Rata)) {
                return "Doar rațele pot participa la curse!";
            }
            RaceEvent race = (RaceEvent) event;
            for (SwimmingDuck s : race.getParticipanti()) {
                if (s.getId().equals(idRata)) {
                    return "Ești deja înscris la acest eveniment!";
                }
            }
            SwimmingDuck sd = getDecorataCompatibila((Rata) user, SwimmingDuck.class);
            if (sd == null) {
                return "Această rață nu are abilitatea de a înota (necesar card/tip swimming)!";
            }
            if (eventDepozit instanceof EventDepozitDB) {
                eventDepozit.adaugaParticipant(idEvent, idRata);
            } else {
                race.adaugaParticipant(sd);
                eventDepozit.modifica(race);
            }

            race.adaugaParticipant(sd);

            notifyObservers(ChangeEventType.EVENT);
            return "Te-ai înscris cu succes!";

        } catch (ValidationException e) {
            return e.getMessage();
        }
    }

    /**
     * Pornește evenimentul ASINCRON.
     * Rulează pe un alt thread, așteaptă 2 secunde, calculează rezultatul, apoi notifică UI-ul.
     */
    public void startEvent(Long idEvent) {
        Event event = eventDepozit.cauta(idEvent);
        if (!"OPEN".equals(event.getStatus())) return;
        event.setStatus("IN_PROGRESS");
        eventDepozit.modifica(event);
        notifyObservers(ChangeEventType.EVENT);

        CompletableFuture.runAsync(() -> {
                    try {

                        Thread.sleep(10000);

                        event.start();

                        eventDepozit.modifica(event);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }, executorService)
                .thenRun(() -> {
                    notifyObservers(ChangeEventType.EVENT);
                });
    }

    /**
     * Returnează istoricul evenimentelor FINISHED la care utilizatorul a fost abonat (observer) sau participant.
     */
    public List<Event> getIstoricEvenimenteUser(Long userId) {
        List<Event> istoric = new ArrayList<>();

        for (Event e : eventDepozit.getDepozit()) {
            if ("FINISHED".equals(e.getStatus())) {
                boolean esteAbonat = false;
                for(IObserver obs : e.getObservers()) {
                    if (obs instanceof User && ((User)obs).getId().equals(userId)) {
                        esteAbonat = true;
                        break;
                    }
                }
                if (!esteAbonat && e instanceof RaceEvent) {
                    for(SwimmingDuck p : ((RaceEvent)e).getParticipanti()) {
                        if (p.getId().equals(userId)) {
                            esteAbonat = true;
                            break;
                        }
                    }
                }
                if (esteAbonat) {
                    istoric.add(e);
                }
            }
        }
        return istoric;
    }


    /**
     * Adaugă un observator (utilizator) la un eveniment.
     * Un observator va fi notificat atunci când evenimentul este pornit sau actualizat.
     * Salvează modificarea în baza de date și notifică interfața.
     *
     * @param idUser ID-ul utilizatorului care vrea să se aboneze
     * @param idEvent ID-ul evenimentului
     * @return mesaj de confirmare sau eroare
     */
    public String adaugaObserver(Long idUser, Long idEvent) {
        try {
            User user = userDepozit.cauta(idUser);
            Event event = eventDepozit.cauta(idEvent);

            for (IObserver o : event.getObservers()) {
                if (o instanceof User && ((User) o).getId().equals(idUser)) {
                    return "Ești deja abonat la acest eveniment!";
                }
            }

            event.subscribe(user);

            eventDepozit.modifica(event);

            notifyObservers(ChangeEventType.EVENT);

            return "Te-ai abonat cu succes!";

        } catch (ValidationException e) {
            return e.getMessage();
        } catch (Exception e) {
            return "Eroare la abonare: " + e.getMessage();
        }
    }

    /**
     * Șterge un eveniment existent din depozit.
     * Nu permite ștergerea dacă evenimentul este în desfășurare.
     *
     * @param id identificatorul evenimentului
     * @return mesaj de confirmare sau eroare
     */
    public String stergeEvent(Long id) {
        try {
            Event event = eventDepozit.cauta(id);

            if ("IN_PROGRESS".equals(event.getStatus())) {
                return "Nu se poate șterge un eveniment aflat în desfășurare!";
            }
            eventDepozit.sterge(id);
            notifyObservers(ChangeEventType.EVENT);

            return "Eveniment șters cu succes!";
        } catch (ValidationException e) {
            return e.getMessage();
        } catch (RuntimeException e) {
            return "Eroare la ștergere: " + e.getMessage();
        }
    }

    /**
     * Elimină un observator (dezabonare) de la un eveniment.
     *
     * @param idUser ID-ul utilizatorului care se dezabonează
     * @param idEvent ID-ul evenimentului
     * @return mesaj de confirmare sau eroare
     */
    public String stergeObserver(Long idUser, Long idEvent) {
        try {
            User user = userDepozit.cauta(idUser);
            Event event = eventDepozit.cauta(idEvent);

            if ("IN_PROGRESS".equals(event.getStatus())) {
                return "Nu te poți dezabona în timpul desfășurării evenimentului!";
            }

            IObserver target = null;
            for (IObserver o : event.getObservers()) {
                if (o instanceof User && ((User) o).getId().equals(idUser)) {
                    target = o;
                    break;
                }
            }

            if (target == null) {
                return "Nu sunteți abonat la acest eveniment!";
            }

            event.unsubscribe(target);

            eventDepozit.modifica(event);

            notifyObservers(ChangeEventType.EVENT);

            return "V-ați dezabonat cu succes!";
        } catch (ValidationException e) {
            return e.getMessage();
        }
    }

    /**
     * Calculează și returnează performanța medie a unui card,
     * împreună cu lista rațelor asociate acestuia.
     * @param id ID-ul cardului
     * @return mesaj cu performanța medie sau eroare
     */
    public String performantaMedieCard(Long id) {
        try{
            Card<? extends Rata> card=sincronizeazaCard(cardDepozit.cauta(id));
            return "Performanta medie: " + card.getPerformantaMedie() + "\n" + "Membrii: " + card.getRate();
        }catch(ValidationException e){
            return e.getMessage();
        }
    }

    /**
     * Adaugă o rață într-un card compatibil (de tip Zburător sau Înotător).
     * @param id1 ID-ul raței
     * @param id2 ID-ul cardului
     * @return mesaj de confirmare sau eroare
     */
    public String adaugaRataCard(Long id1, Long id2){
        try{
            User user=userDepozit.cauta(id1);
            Card<? extends Rata> card=sincronizeazaCard(cardDepozit.cauta(id2));
            if(!(user instanceof Rata rata)){
                return "Acest id nu apartine unei rate!";
            }
            for(Rata r : card.getRate()) {
                if(r.getId().equals(rata.getId())) {
                    return "Rata apartine deja acestui card!";
                }
            }
            if (card.getTip().equals("Zburator")) {
                FlyingDuck fd = getDecorataCompatibila(rata, FlyingDuck.class);
                if (fd == null) return "Rata nu este compatibila cu acest card!";
                ((FlyingCard) card).adaugaRata(fd);
                if(rata.getIdCard() != -1L){
                    Card<? extends Rata> cardS=sincronizeazaCard(cardDepozit.cauta(rata.getIdCard()));
                    ((FlyingCard) cardS).stergeRata(fd);
                }
                rata.setIdCard(card.getId());
                userDepozit.modifica(rata);
            } else if (card.getTip().equals("Inotator")) {
                SwimmingDuck sd = getDecorataCompatibila(rata, SwimmingDuck.class);
                if (sd == null) return "Rata nu este compatibila cu acest card!";
                ((SwimmingCard) card).adaugaRata(sd);
                if(rata.getIdCard() != -1L){
                    Card<? extends Rata> cardS=sincronizeazaCard(cardDepozit.cauta(rata.getIdCard()));
                    ((SwimmingCard) cardS).stergeRata(sd);
                }
                rata.setIdCard(card.getId());
                userDepozit.modifica(rata);
                notifyObservers(ChangeEventType.CARD);
            } else {
                return "Rata nu este compatibila cu acest card!";
            }
        }catch(ValidationException e){
            return e.getMessage();
        }
        return "Rata adaugata cu succes in card!";
    }

    /**
     * Elimină o rață dintr-un card.
     * @param id1 ID-ul raței
     * @param id2 ID-ul cardului
     * @return mesaj de confirmare sau eroare
     */
    public String stergeRataCard(Long id1, Long id2){
        try{
            User user=userDepozit.cauta(id1);
            Card<? extends Rata> card=sincronizeazaCard(cardDepozit.cauta(id2));
            if(!(user instanceof Rata rata)){
                return "Acest id nu apartine unei rate!";
            }
            boolean gasit = false;
            for(Rata r : card.getRate()) {
                if(r.getId().equals(rata.getId())) {
                    gasit = true;
                    break;
                }
            }
            if(!gasit){
                return "Rata nu apartine acestui card!";
            }
            if (card.getTip().equals("Zburator")) {
                FlyingDuck fd = getDecorataCompatibila(rata, FlyingDuck.class);
                if (fd != null) {
                    ((FlyingCard) card).stergeRata(fd);
                    rata.setIdCard(-1L);
                    userDepozit.modifica(rata);
                }
            } else if (card.getTip().equals("Inotator")) {
                SwimmingDuck sd = getDecorataCompatibila(rata, SwimmingDuck.class);
                if (sd != null) {
                    ((SwimmingCard) card).stergeRata(sd);
                    rata.setIdCard(-1L);
                    userDepozit.modifica(rata);
                }
            }
            notifyObservers(ChangeEventType.CARD);
        }catch(ValidationException e){
            return e.getMessage();
        }
        return "Rata stearsa cu succes din card!";
    }

    public List<Card<? extends Rata>> getToateCardurile() {
        List<Card<? extends Rata>> carduri = new ArrayList<>(cardDepozit.getDepozit());
        for (Card<? extends Rata> c : carduri) {
            sincronizeazaCard(c);
        }
        return carduri;
    }

    /**
     * Adaugă un card nou în sistem, de tip înotător sau zburător.
     * @param tip tipul cardului ("SWIMMINGCARD" sau "FLYINGCARD")
     * @param id identificatorul cardului
     * @param nume numele cardului
     * @return mesaj de confirmare sau eroare
     */
    public String adaugaCard(String tip, Long id, String nume){
        validator = new CardValidator();
        if(Objects.equals(tip, "SWIMMINGCARD")){
            Card<SwimmingDuck> card =  new SwimmingCard(id, nume, "Inotator");
            try {
                validator.valideaza(card);
                cardDepozit.adauga(card);
                notifyObservers(ChangeEventType.CARD);
                return "Card adaugat cu succes!";
            }catch(ValidationException e){
                return e.getMessage();
            }
        }
        else if(Objects.equals(tip, "FLYINGCARD")){
            Card<FlyingDuck> card = new FlyingCard(id, nume, "Zburator");
            try {
                validator.valideaza(card);
                cardDepozit.adauga(card);
                notifyObservers(ChangeEventType.CARD);
                return "Card adaugat cu succes!";
            }catch(ValidationException e){
                return e.getMessage();
            }
        }
        return "Tip invalid de card";
    }


    /**
     * Șterge un card existent și resetează idCard-ul rațelor asociate.
     * @param id identificatorul cardului
     * @return mesaj de confirmare sau eroare
     */
    public String stergeCard(Long id){
        try {
            Card<? extends Rata> card=sincronizeazaCard(cardDepozit.cauta(id));
            for(Rata r : card.getRate()) {
                User user=userDepozit.cauta(r.getId());
                Rata rata = (Rata) user;
                rata.setIdCard(-1L);
                userDepozit.modifica(rata);
            }
            card.getRate().clear();
            cardDepozit.sterge(id);
            notifyObservers(ChangeEventType.CARD);
            return "Card sters cu succes!";
        }catch(ValidationException e){
            return e.getMessage();
        }
    }

    /**
     * Adaugă o persoană nouă în sistem după validare.
     * @param id identificatorul persoanei
     * @param username numele de utilizator
     * @param email adresa de email
     * @param password parola
     * @param nume numele real
     * @param prenume prenumele real
     * @param dataNasterii data nașterii
     * @param ocupatie ocupația
     * @return un mesaj care confirmă adăugarea sau explică eroarea
     */

    public String adaugaPersoana(Long id, String username, String email, String password, String nume, String prenume, String dataNasterii, String ocupatie){
        Persoana persoana = new Persoana(id, username, email, password, nume, prenume, dataNasterii, ocupatie);
        validator = new PersoanaValidator();
        try {
            validator.valideaza(persoana);
            try {
                userDepozit.adauga(persoana);
            }catch(ValidationException e){
                return e.getMessage();
            }
        } catch (ValidationException e) {
            return e.getMessage();
        }
        notifyObservers(ChangeEventType.USER);
        return "Persoana adaugata cu succes!";
    }

    /**
     * Adaugă o rata nouă în sistem după validare.
     * @param id identificatorul raței
     * @param username numele de utilizator
     * @param email adresa de email
     * @param password parola
     * @param rezistenta rezistența fizică a raței
     * @param viteza viteza maximă
     * @param tip tipul raței (FLYING, SWIMMING, FLYING_AND_SWIMMING)
     * @return un mesaj care confirmă adăugarea sau explică eroarea
     */

    public String adaugaRata(Long id, String username, String email, String password, Double rezistenta, Double viteza,  String tip){
        Rata rata;
        try {
            rata = DuckFactory.creeazaRata(id, username, email, password, rezistenta, viteza, tip);
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
        validator = new RataValidator();
        try {
            validator.valideaza(rata);
            try {
                rata.setIdCard(-1L);
                userDepozit.adauga(rata);
            }catch(ValidationException e){
                return e.getMessage();
            }
        } catch (ValidationException e) {
            return e.getMessage();
        }
        notifyObservers(ChangeEventType.USER);
        return "Rata adaugata cu succes!";
    }

    /**
     * Șterge un User existent din sistem.
     * @param id identificatorul raței
     * @return un mesaj care confirmă eliminarea sau explică eroarea
     */

    public String stergeUser(Long id){
        try {
            for (User u : userDepozit.cauta(id).getPrieteni()) {
                prietenieDepozit.sterge(id, u.getId());
            }
            User u = userDepozit.cauta(id);
            for (Event e : eventDepozit.getDepozit()) {
                for (IObserver o : e.getObservers()) {
                    if (o instanceof User) {
                        if (((User) o).getId().equals(u.getId())) {
                            e.unsubscribe(u);
                            eventDepozit.modifica(e);
                            break;
                        }
                    }
                }
            }
            if(u instanceof Rata r){
                if(r.getIdCard() != -1L) {
                    Card<? extends Rata> card = sincronizeazaCard(cardDepozit.cauta(r.getIdCard()));
                    if (card.getTip().equals("Zburator")) {
                        FlyingDuck fd = getDecorataCompatibila(r, FlyingDuck.class);
                        if (fd != null) {
                            ((FlyingCard) card).stergeRata(fd);
                        }
                    } else if (card.getTip().equals("Inotator")) {
                        SwimmingDuck sd = getDecorataCompatibila(r, SwimmingDuck.class);
                        if (sd != null) {
                            ((SwimmingCard) card).stergeRata(sd);
                        }
                    }
                }
            }
            userDepozit.sterge(id);
        }catch(ValidationException e){
            return e.getMessage();
        }
        notifyObservers(ChangeEventType.USER);
        return "User eliminat cu succes!";
    }

    /**
     * Creează o prietenie între doi utilizatori existenți.
     * @param id1 ID-ul primului utilizator
     * @param id2 ID-ul celui de-al doilea utilizator
     * @return mesaj de confirmare sau eroare
     */

    public String adaugaPrietenie(Long id1, Long id2){
        try{
            User user1=userDepozit.cauta(id1);
            User user2=userDepozit.cauta(id2);
            initPrietenii();
            for (User u : user1.getPrieteni()) {
                if(u.getId().equals(user2.getId())){
                    return "Aceasta prietenie exista deja!";
                }
            }
            user1.adaugaPrieten(user2);
            user2.adaugaPrieten(user1);
            try {
                prietenieDepozit.adauga(new Prietenie(id1, id2));
            }catch (ValidationException e){
                return e.getMessage();
            }
        }catch(ValidationException e){
            return e.getMessage();
        }
        notifyObservers(ChangeEventType.FRIENDSHIP);
        return "Prietenie adaugata cu succes!";
    }

    /**
     * Elimină o prietenie între doi utilizatori.
     *
     * @param id1 ID-ul primului utilizator
     * @param id2 ID-ul celui de-al doilea utilizator
     * @return mesaj de confirmare sau eroare
     */

    public String stergePrietenie(Long id1, Long id2){
        try{
            User user1=userDepozit.cauta(id1);
            User user2=userDepozit.cauta(id2);
//            initPrietenii();
//            boolean gasit = false;
//            for (User u : user1.getPrieteni()) {
//                if(u.getId().equals(user2.getId())){
//                    gasit = true;
//                    break;
//                }
//            }
//            if(!gasit){
//                return "Aceasta prietenie nu exista!";
//            }
            user1.stergePrieten(user2);
            user2.stergePrieten(user1);
            try {
                prietenieDepozit.sterge(id1, id2);
            }catch(ValidationException e){
                return e.getMessage();
            }
        }catch(ValidationException e){
            return e.getMessage();
        }
        notifyObservers(ChangeEventType.FRIENDSHIP);
        return "Prietenie eliminata cu succes!";
    }

    /**
     * Determină numărul total de comunități (componente conexe) din rețea.
     * @return numărul de comunități
     */

    public Integer numarComunitati(){
        Set<User> vizitate = new HashSet<>();
        Integer numarComunitati = 0;
        //for(User u: userDepozit.getDepozit()){
        for(User u: getUseriCuPrieteni()){
            if(!vizitate.contains(u)){
                numarComunitati++;
                dfs(u, vizitate);
            }
        }
        return numarComunitati;
    }

    /**
     * Parcurgere în adâncime (DFS) pentru marcarea utilizatorilor vizitați.
     * @param u utilizatorul curent
     * @param vizitate setul de utilizatori vizitați
     */

    private void dfs(User u, Set<User> vizitate){
        vizitate.add(u);
        for(User f: u.getPrieteni()){
            if(!vizitate.contains(f)) dfs(f, vizitate);
        }
    }

    /**
     * Găsește comunitatea cea mai sociabilă (cu diametrul cel mai mare).
     * @return lista ID-urilor utilizatorilor din comunitatea cea mai sociabilă
     */

    public List<Long> ceaMaiSociabilaComunitate() {
        //List<User> toti = new ArrayList<>(userDepozit.getDepozit());
        List<User> toti = new ArrayList<>(getUseriCuPrieteni());
        Set<User> vizitat = new HashSet<>();
        int maxDiametru = -1;
        List<User> ceaMaiSociabila = new ArrayList<>();
        for (User u : toti) {
            if (!vizitat.contains(u)) {
                List<User> componenta = getComponenta(u, vizitat);
                int diametru = calculeazaDiametru(componenta);
                if (diametru > maxDiametru) {
                    maxDiametru = diametru;
                    ceaMaiSociabila = componenta;
                }
            }
        }
        List<Long> rezultat = new ArrayList<>();
        for (User u : ceaMaiSociabila) {
            rezultat.add(u.getId());
        }
        return rezultat;
    }


    /**
     * Obține toți utilizatorii din aceeași comunitate (componentă conexă) cu utilizatorul dat.
     * @param start utilizatorul de pornire
     * @param vizitat setul global de utilizatori vizitați
     * @return lista utilizatorilor din comunitate
     */

    private List<User> getComponenta(User start, Set<User> vizitat) {
        List<User> componenta = new ArrayList<>();
        Queue<User> coada = new LinkedList<>();
        coada.add(start);
        vizitat.add(start);
        while (!coada.isEmpty()) {
            User curent = coada.poll();
            componenta.add(curent);
            for (User prieten : curent.getPrieteni()) {
                if (!vizitat.contains(prieten)) {
                    vizitat.add(prieten);
                    coada.add(prieten);
                }
            }
        }

        return componenta;
    }

    /**
     * Calculează diametrul unei componente (lungimea celei mai lungi căi între doi membri).
     * @param componenta lista utilizatorilor din comunitate
     * @return valoarea diametrului
     */

    private int calculeazaDiametru(List<User> componenta) {
        int diametru = 0;
        for (User u : componenta) {
            int ecc = bfs(u, componenta);
            diametru = Math.max(diametru, ecc);
        }
        return diametru;
    }

    /**
     * Cautare în lățime (BFS) pentru a calcula excentricitatea unui utilizator.
     * @param src utilizatorul sursă
     * @param comp componenta conexă
     * @return cea mai mare distanță (excentricitatea)
     */

    private int bfs(User src, List<User> comp) {
        Set<User> compSet = new HashSet<>(comp);
        Queue<User> coada = new LinkedList<>();
        Map<User, Integer> dist = new HashMap<>();
        coada.add(src);
        dist.put(src, 0);
        while (!coada.isEmpty()) {
            User curent = coada.poll();
            for (User u : curent.getPrieteni()) {
                if (!compSet.contains(u)) continue;
                if (!dist.containsKey(u)) {
                    dist.put(u, dist.get(curent) + 1);
                    coada.add(u);
                }
            }
        }
        int max = 0;
        for (Integer d : dist.values()) {
            if (d > max) max = d;
        }
        return max;
    }

//    private boolean containsInterface(Rata rata, Class<?> iface) {
//        if (rata == null) return false;
//        if (iface.isInstance(rata)) return true;
//        if (rata instanceof RataDecorator) {
//            return containsInterface(((RataDecorator) rata).getDecorata(), iface);
//        }
//        return false;
//    }

    /**
     * Metodă generică recursivă care caută într-o structură decorată
     * un obiect compatibil cu o anumită interfață sau clasă.
     * @param <T> tipul interfeței/clasei căutate
     * @param rata rața decorată
     * @param iface interfața sau clasa compatibilă
     * @return instanța compatibilă sau {@code null} dacă nu există
     */
    private <T> T getDecorataCompatibila(Rata rata, Class<T> iface) {
        if (iface.isInstance(rata)) {
            return iface.cast(rata);
        } else if (rata instanceof RataDecorator) {
            return getDecorataCompatibila(((RataDecorator) rata).getDecorata(), iface);
        }
        return null;
    }

    public Collection<User> getUseriCuPrieteni() {
        Collection<User> useri = userDepozit.getDepozit();

        Collection<Relatie> prietenii = prietenieDepozit.getDepozit();

        Map<Long, User> userMap = useri.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        for (Relatie r : prietenii) {
            User u1 = userMap.get(r.getId1());
            User u2 = userMap.get(r.getId2());

            if (!u1.getPrieteni().contains(u2)) {
                u1.adaugaPrieten(u2);
            }
            if (!u2.getPrieteni().contains(u1)) {
                u2.adaugaPrieten(u1);
            }
        }

        return useri;
    }

    /**
     * Sincronizează un card cu toate rațele care îl au ca idCard în userDepozit.
     * @param card cardul care trebuie sincronizat
     * @return cardul sincronizat cu toate rațele aferente
     */
    public Card<? extends Rata> sincronizeazaCard(Card<? extends Rata> card) {
        for (User u : userDepozit.getDepozit()) {
            if (u instanceof Rata r) {

                // Dacă rata are idCard egal cu cardul curent
                if (r.getIdCard() != -1L && r.getIdCard().equals(card.getId())) {
                    if (card.getTip().equals("Zburator")) {
                        FlyingDuck fd = getDecorataCompatibila(r, FlyingDuck.class);
                        if (fd != null) {
                            ((FlyingCard) card).adaugaRata(fd);
                        }
                    } else if (card.getTip().equals("Inotator")) {
                        SwimmingDuck sd = getDecorataCompatibila(r, SwimmingDuck.class);
                        if (sd != null) {
                            ((SwimmingCard) card).adaugaRata(sd);
                        }
                    }
                }
            }
        }
        return card;
    }

//    public List<Rata> getRate(TipRata tip) {
//        return userDepozit.getRate(tip);
//    }

    /**
     * Paginează și filtrează utilizatorii, folosind interfața Pageable.
     * @param pageable Obiect ce conține numărul și dimensiunea paginii.
     * @param tipUser Tipul de utilizator pentru filtrare ("PERSOANA", "RATA", sau null pentru toți).
     * @return Un obiect Page<User> care conține lista și metadatele de paginare.
     */
    public Page<User> getPaginaUseri(Pageable pageable, String tipUser) {
        return userDepozit.findAllFiltered(pageable, tipUser);
    }

//    /**
//     * Returnează numărul total de utilizatori, opțional filtrat după tip.
//     * @param tipUser Tipul de utilizator pentru filtrare ("PERSOANA", "RATA", sau null pentru toți).
//     * @return Numărul total de utilizatori.
//     */
//    public int getTotalUserCount(String tipUser) {
//        return userDepozit.countFiltered(tipUser);
//    }

    /**
     * Paginează relațiile (prieteniile).
     * @param pageable Obiect ce conține numărul și dimensiunea paginii.
     * @return Un obiect Page<Relatie> care conține lista și metadatele de paginare.
     */
    public Page<Relatie> getPaginaPrietenii(Pageable pageable) {
        return prietenieDepozit.findAll(pageable);
    }

    public Page<Rata> getPaginaRateByTip(Pageable pageable, TipRata tip) {
        return userDepozit.findAllRateByTip(pageable, tip);
    }

}
