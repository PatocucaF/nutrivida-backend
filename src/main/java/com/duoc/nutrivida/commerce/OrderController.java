package com.duoc.nutrivida.commerce;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@PreAuthorize("hasRole('CLIENTE')")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse checkout(Authentication authentication) {
        return orderService.checkout(authentication.getName());
    }

    @GetMapping
    public PagedModel<OrderResponse> list(Authentication authentication,
                                          @ParameterObject @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        Page<OrderResponse> page = orderService.listCustomerOrders(authentication.getName(), pageable);
        return new PagedModel<>(page);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Long id, Authentication authentication) {
        return orderService.getCustomerOrder(authentication.getName(), id);
    }
}
