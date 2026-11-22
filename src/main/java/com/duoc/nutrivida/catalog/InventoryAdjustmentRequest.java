package com.duoc.nutrivida.catalog;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InventoryAdjustmentRequest(
        @Min(1) int quantity,
        @NotNull InventoryMovementType type,
        @Size(max = 255) String note
) {
}
