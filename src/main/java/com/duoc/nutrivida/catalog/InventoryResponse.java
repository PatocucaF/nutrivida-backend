package com.duoc.nutrivida.catalog;

import java.time.LocalDateTime;

public record InventoryResponse(
        Long movementId,
        Long productId,
        String sku,
        String productName,
        int stock,
        InventoryMovementType type,
        int quantityChange,
        String note,
        String actorEmail,
        LocalDateTime createdAt
) {
    public static InventoryResponse from(InventoryMovement movement) {
        Product product = movement.getProduct();
        return new InventoryResponse(movement.getId(), product.getId(), product.getSku(), product.getName(),
                product.getStock(), movement.getType(), movement.getQuantityChange(), movement.getNote(),
                movement.getActorEmail(), movement.getCreatedAt());
    }
}
