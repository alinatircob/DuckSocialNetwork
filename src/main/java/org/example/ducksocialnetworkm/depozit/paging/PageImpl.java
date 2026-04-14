package org.example.ducksocialnetworkm.depozit.paging;

import org.example.ducksocialnetworkm.domeniu.user.User;
import java.util.List;

/**
 * Implementarea concretă a interfeței Page.
 * @param <T> Tipul elementelor din pagină.
 */
public class PageImpl<T> implements Page<T> {
    private final List<T> content;
    private final long totalElements;
    private final Pageable pageable;

    public PageImpl(List<T> content, Pageable pageable, long totalElements) {
        this.content = content;
        this.totalElements = totalElements;
        this.pageable = pageable;
    }

    @Override
    public List<T> getContent() {
        return content;
    }

    @Override
    public long getTotalElements() {
        return totalElements;
    }

    @Override
    public int getTotalPages() {
        if (pageable.getPageSize() == 0) return 0;
        return (int) Math.ceil((double) totalElements / pageable.getPageSize());
    }

    @Override
    public int getPageSize() {
        return pageable.getPageSize();
    }

    @Override
    public int getPageNumber() {
        return pageable.getPageNumber();
    }
}