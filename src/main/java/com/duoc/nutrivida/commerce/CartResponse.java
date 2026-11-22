package com.duoc.nutrivida.commerce;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(List<CartLineResponse> items, BigDecimal total) {
    public static CartResponse from(List<CartItem> items) {
        List<CartLineResponse> lines = items.stream().map(CartLineResponse::from).toList();
        BigDecimal total = lines.stream().map(CartLineResponse::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(lines, total);
    }
}
