package org.example.ducksocialnetworkm.depozit.paging;

import org.example.ducksocialnetworkm.depozit.Depozit;
import org.example.ducksocialnetworkm.domeniu.relatie.Relatie;

/**
 * Definește operațiunile de paginare specifice pentru relații (Prietenii).
 */
public interface PagingRelatieRepository extends Depozit<Relatie> {

    /**
     * Returnează o pagină de relații.
     * @param pageable Obiectul ce conține informațiile de paginare.
     * @return O pagină de Relatie.
     */
    Page<Relatie> findAll(Pageable pageable);

    /**
     * Returnează numărul total de relații.
     * @return Numărul total de relații.
     */
    int countAll();
}