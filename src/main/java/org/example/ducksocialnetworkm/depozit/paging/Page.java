package org.example.ducksocialnetworkm.depozit.paging;

import java.util.List;

/**
 * Conține un subset de elemente și informațiile de paginare aferente.
 * @param <T> Tipul elementelor din pagină.
 */
public interface Page<T> {
    /**
     * @return Lista de elemente pentru pagina curentă.
     */
    List<T> getContent();

    /**
     * @return Numărul total de elemente din setul complet de rezultate (nefiltrat de pagină).
     */
    long getTotalElements();

    /**
     * @return Numărul total de pagini.
     */
    int getTotalPages();

    /**
     * @return Dimensiunea paginii curente.
     */
    int getPageSize();

    /**
     * @return Numărul paginii curente (începând de la 0).
     */
    int getPageNumber();
}