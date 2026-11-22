package com.duoc.nutrivida.commerce;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cart")
@PreAuthorize("hasRole('CLIENTE')")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartResponse get(Authentication authentication) {
        return cartService.get(authentication.getName());
    }

    @PostMapping("/items/{productId}")
    public CartResponse add(@PathVariable Long productId, @Valid @RequestBody CartItemRequest request,
                            Authentication authentication) {
        return cartService.add(authentication.getName(), productId, request.quantity());
    }

    @PutMapping("/items/{productId}")
    public CartResponse update(@PathVariable Long productId, @Valid @RequestBody CartItemRequest request,
                               Authentication authentication) {
        return cartService.add(authentication.getName(), productId, request.quantity());
    }

    @DeleteMapping("/items/{productId}")
    public CartResponse remove(@PathVariable Long productId, Authentication authentication) {
        return cartService.remove(authentication.getName(), productId);
    }

    @DeleteMapping
    public void clear(Authentication authentication) {
        cartService.clear(authentication.getName());
    }
}
