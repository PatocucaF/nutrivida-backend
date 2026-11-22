package com.duoc.nutrivida.commerce;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(Long id, String customerEmail, OrderStatus status, BigDecimal total,
                            LocalDateTime createdAt, List<OrderLineResponse> items) {
    public static OrderResponse from(CustomerOrder order) {
        return new OrderResponse(order.getId(), order.getCustomer().getEmail(), order.getStatus(), order.getTotal(),
                order.getCreatedAt(), order.getItems().stream().map(OrderLineResponse::from).toList());
    }
}
