package com.ecommerce.superadmin.common;

import java.util.List;

public record PageResponse<T>(List<T> items, int page, int size, long totalItems, int totalPages) {

    public static <T> PageResponse<T> of(List<T> items, PageRequest request, long totalItems) {
        int totalPages = (int) Math.ceil(totalItems / (double) request.size());
        return new PageResponse<>(items, request.page(), request.size(), totalItems, totalPages);
    }
}
