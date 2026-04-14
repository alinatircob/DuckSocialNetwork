package org.example.ducksocialnetworkm.depozit.paging;

/**
 * Definește o cerere de paginare, incluzând numărul paginii și dimensiunea acesteia.
 */
public interface Pageable {
    /**
     * @return Indexul paginii (începând de la 0).
     */
    int getPageNumber();

    /**
     * @return Numărul maxim de elemente pe pagină.
     */
    int getPageSize();

    /**
     * @return Offset-ul (poziția de start) în setul de rezultate.
     */
    long getOffset();
}