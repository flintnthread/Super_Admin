package com.ecommerce.superadmin.common;

public record PageRequest(int page, int size) {

    private static final int MAX_SIZE = 100;

    public static PageRequest of(Integer page, Integer size) {
        int safePage = page == null || page < 0 ? 0 : page;
        int safeSize = size == null || size < 1 ? 20 : Math.min(size, MAX_SIZE);
        return new PageRequest(safePage, safeSize);
    }

    public int offset() {
        return page * size;
    }
}
