package com.duoc.nutrivida.commerce;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    Optional<Sale> findByOrderId(Long orderId);
    Page<Sale> findAllByOrderBySoldAtDesc(Pageable pageable);
}
