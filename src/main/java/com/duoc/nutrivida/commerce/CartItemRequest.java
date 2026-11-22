package com.duoc.nutrivida.commerce;

import jakarta.validation.constraints.Min;

public record CartItemRequest(@Min(1) int quantity) {
}
