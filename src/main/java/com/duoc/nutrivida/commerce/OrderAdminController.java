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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/orders")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Pedidos (administración)", description = "Consulta de todos los pedidos (ADMIN y VENDEDOR). Anular pedidos requiere ADMIN")
public class OrderAdminController {

    private final OrderService orderService;

    public OrderAdminController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Lista todos los pedidos", description = "Listado paginado de pedidos de todos los clientes. Roles: ADMIN y VENDEDOR")
    @ApiResponse(responseCode = "200", description = "Listado entregado")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "No tiene el rol requerido")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public PagedModel<OrderResponse> list(
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        Page<OrderResponse> page = orderService.listAll(pageable);
        return new PagedModel<>(page);
    }

    @Operation(summary = "Detalle de un pedido", description = "Devuelve cualquier pedido con sus líneas. Roles: ADMIN y VENDEDOR")
    @ApiResponse(responseCode = "200", description = "Pedido encontrado")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "No tiene el rol requerido")
    @ApiResponse(responseCode = "404", description = "El pedido no existe")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public OrderResponse get(@PathVariable Long id) {
        return orderService.getAnyOrder(id);
    }

    @Operation(summary = "Anula un pedido", description = "Requiere ADMIN. Restituye el stock del pedido")
    @ApiResponse(responseCode = "200", description = "Pedido anulado")
    @ApiResponse(responseCode = "400", description = "El pedido ya estaba anulado")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "No tiene el rol requerido")
    @ApiResponse(responseCode = "404", description = "El pedido no existe")
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public OrderResponse cancel(@PathVariable Long id, Authentication authentication) {
        return orderService.cancel(id, authentication.getName());
    }
}
