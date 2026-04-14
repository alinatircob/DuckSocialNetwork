package org.example.ducksocialnetworkm.serviciu;

import java.util.List;

/**
 * Interfața {@code IServiciu} definește funcționalitățile principale ale sistemului,
 * permițând gestionarea rațelor, cardurilor, utilizatorilor, prieteniilor și evenimentelor.
 * Această interfață este implementată de clasa {@link Serviciu},
 * care oferă logica de bază pentru operațiile aplicației.
 */

public interface IServiciu {

    public String adaugaRataCard(Long id1, Long id2);

    public String stergeRataCard(Long id1, Long id2);

    public String adaugaCard(String tip, Long id, String nume);

    public String stergeCard(Long id);

    public String adaugaPersoana(Long id, String username, String email, String password, String nume, String prenume, String dataNasterii, String ocupatie);

    public String adaugaRata(Long id, String username, String email, String password, Double rezistenta, Double viteza,  String tip);

    public String stergeUser(Long id);

    public String adaugaPrietenie(Long id1, Long id2);

    public String stergePrietenie(Long id1, Long id2);

    public Integer numarComunitati();

    public List<Long> ceaMaiSociabilaComunitate();

    public String performantaMedieCard(Long id);

    public String adaugaEvent(Long id, String nume, List<Double> distante, Long idCreator);

    public String stergeEvent(Long id);

    public String adaugaObserver(Long id1, Long id2);

    public String stergeObserver(Long id1, Long id2);

    public void startEvent(Long id);

}
