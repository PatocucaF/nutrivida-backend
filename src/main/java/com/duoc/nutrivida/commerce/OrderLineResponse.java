package com.duoc.nutrivida.commerce;

import java.math.BigDecimal;

public record OrderLineResponse(Long productId, String sku, String productName, int quantity,
                                BigDecimal unitPrice, BigDecimal subtotal) {
    public static OrderLineResponse from(OrderItem item) {
        return new OrderLineResponse(item.getProduct().getId(), item.getSku(), item.getProductName(),
                item.getQuantity(), item.getUnitPrice(),
                item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
    }
}
