package org.example.ducksocialnetworkm.depozit.paging;

import org.example.ducksocialnetworkm.domeniu.user.User;

public interface UserRepository extends PagingRepository<User> {

    /**
     * Returnează o pagină de entități, opțional filtrată după tipul de utilizator.
     * @param pageable Obiectul ce conține informațiile de paginare.
     * @param tipUser Tipul de utilizator pentru filtrare ("PERSOANA", "RATA", sau null pentru toți).
     * @return O pagină de utilizatori.
     */
    Page<User> findAllFiltered(Pageable pageable, String tipUser);

    /**
     * Returnează numărul total de utilizatori, opțional filtrat după tip.
     * @param tipUser Tipul de utilizator pentru filtrare ("PERSOANA", "RATA", sau null pentru toți).
     * @return Numărul total de utilizatori.
     */
    int countFiltered(String tipUser);
}
