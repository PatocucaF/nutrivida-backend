package com.duoc.nutrivida.catalog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Inventario", description = "Movimientos de stock. Requiere rol ADMIN")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Operation(summary = "Ajusta el stock de un producto", description = "Registra un movimiento de inventario (por ejemplo, reposición) y actualiza el stock")
    @ApiResponse(responseCode = "200", description = "Ajuste registrado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o tipo de movimiento no permitido")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "No tiene el rol requerido")
    @ApiResponse(responseCode = "404", description = "El producto no existe")
    @PostMapping("/products/{productId}/adjustments")
    public InventoryResponse adjust(@PathVariable Long productId,
                                    @Valid @RequestBody InventoryAdjustmentRequest request,
                                    Authentication authentication) {
        return inventoryService.adjust(productId, request, authentication.getName());
    }

    @Operation(summary = "Historial de movimientos", description = "Lista los movimientos de inventario de un producto")
    @ApiResponse(responseCode = "200", description = "Historial entregado")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "No tiene el rol requerido")
    @ApiResponse(responseCode = "404", description = "El producto no existe")
    @GetMapping("/products/{productId}/movements")
    public List<InventoryResponse> history(@PathVariable Long productId) {
        return inventoryService.history(productId);
    }
}
