package com.duoc.nutrivida.catalog;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@PreAuthorize("hasRole('ADMIN')")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/products/{productId}/adjustments")
    public InventoryResponse adjust(@PathVariable Long productId,
                                    @Valid @RequestBody InventoryAdjustmentRequest request,
                                    Authentication authentication) {
        return inventoryService.adjust(productId, request, authentication.getName());
    }

    @GetMapping("/products/{productId}/movements")
    public List<InventoryResponse> history(@PathVariable Long productId) {
        return inventoryService.history(productId);
    }
}
