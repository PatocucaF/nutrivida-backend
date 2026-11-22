package com.duoc.nutrivida.catalog;

public record CategoryResponse(Long id, String name, String description, boolean active) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription(),
                category.isActive());
    }
}
