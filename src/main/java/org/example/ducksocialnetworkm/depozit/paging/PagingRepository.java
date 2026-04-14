package org.example.ducksocialnetworkm.depozit.paging;

import org.example.ducksocialnetworkm.depozit.Depozit;
import org.example.ducksocialnetworkm.domeniu.user.User;

/**
 * Definește operațiunile de paginare pentru un depozit.
 * @param <E> Tipul entității.
 */
public interface PagingRepository<E> extends Depozit<E> {

    /**
     * Returnează o pagină de entități.
     * @param pageable Obiectul ce conține informațiile de paginare.
     * @return O pagină de entități.
     */
    Page<E> findAll(Pageable pageable);
}