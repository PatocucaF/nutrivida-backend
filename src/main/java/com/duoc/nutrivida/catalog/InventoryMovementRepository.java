package com.duoc.nutrivida.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
    List<InventoryMovement> findTop100ByProductIdOrderByCreatedAtDesc(Long productId);
}
