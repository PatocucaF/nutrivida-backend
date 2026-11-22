package com.duoc.nutrivida.commerce;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
    List<CartItem> findByCartIdOrderByProductId(Long cartId);
    void deleteAllByCartId(Long cartId);
}
