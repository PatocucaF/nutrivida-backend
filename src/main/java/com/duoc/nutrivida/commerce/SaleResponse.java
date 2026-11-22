package com.duoc.nutrivida.commerce;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SaleResponse(Long id, Long orderId, String customerEmail, BigDecimal total, SaleStatus status,
                           LocalDateTime soldAt) {
    public static SaleResponse from(Sale sale) {
        return new SaleResponse(sale.getId(), sale.getOrder().getId(), sale.getOrder().getCustomer().getEmail(),
                sale.getTotal(), sale.getStatus(), sale.getSoldAt());
    }
}
