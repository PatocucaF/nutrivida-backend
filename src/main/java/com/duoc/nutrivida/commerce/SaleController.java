package com.duoc.nutrivida.commerce;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sales")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Ventas", description = "Reporte de ventas. Requiere rol ADMIN")
public class SaleController {

    private final SaleRepository saleRepository;

    public SaleController(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    @Operation(summary = "Lista ventas", description = "Listado paginado de ventas, de la más reciente a la más antigua")
    @ApiResponse(responseCode = "200", description = "Listado entregado")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "No tiene el rol requerido")
    @GetMapping
    public PagedModel<SaleResponse> list(
            @ParameterObject @PageableDefault(size = 10, sort = "soldAt") Pageable pageable) {
        Page<SaleResponse> page = saleRepository.findAllByOrderBySoldAtDesc(pageable).map(SaleResponse::from);
        return new PagedModel<>(page);
    }
}
