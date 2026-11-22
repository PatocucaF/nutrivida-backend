package com.duoc.nutrivida.commerce;

import java.math.BigDecimal;

public record CartLineResponse(Long productId, String sku, String name, BigDecimal unitPrice,
                               int quantity, BigDecimal subtotal, int availableStock) {
    public static CartLineResponse from(CartItem item) {
        var product = item.getProduct();
        return new CartLineResponse(product.getId(), product.getSku(), product.getName(), product.getPrice(),
                item.getQuantity(), product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())),
                product.getStock());
    }
}
