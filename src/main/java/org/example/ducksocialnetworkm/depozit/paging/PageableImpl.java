package org.example.ducksocialnetworkm.depozit.paging;

/**
 * Implementarea concretă a interfeței Pageable.
 */
public class PageableImpl implements Pageable {
    private final int pageNumber;
    private final int pageSize;

    public PageableImpl(int pageNumber, int pageSize) {
        this.pageNumber = Math.max(0, pageNumber);
        this.pageSize = Math.max(1, pageSize);
    }

    @Override
    public int getPageNumber() {
        return pageNumber;
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public long getOffset() {
        return (long) pageNumber * pageSize;
    }
}