package com.chordsked.backend.common;

import java.util.List;

public class PageResult<T> {
    private long total;
    private List<T> items;

    public PageResult() {
    }

    public PageResult(long total, List<T> items) {
        this.total = total;
        this.items = items;
    }

    public static <T> PageResult<T> of(long total, List<T> items) {
        return new PageResult<>(total, items);
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }
}

