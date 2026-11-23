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
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Pedidos del cliente", description = "Compra y consulta de pedidos propios. Requiere rol CLIENTE")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Confirma la compra", description = "Convierte el carrito en un pedido y descuenta el stock")
    @ApiResponse(responseCode = "201", description = "Pedido creado")
    @ApiResponse(responseCode = "400", description = "Carrito vacío, producto no disponible o stock insuficiente")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "No tiene el rol requerido")
    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse checkout(Authentication authentication) {
        return orderService.checkout(authentication.getName());
    }

    @Operation(summary = "Lista mis pedidos", description = "Listado paginado de los pedidos del cliente autenticado")
    @ApiResponse(responseCode = "200", description = "Listado entregado")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "No tiene el rol requerido")
    @GetMapping
    public PagedModel<OrderResponse> list(Authentication authentication,
                                          @ParameterObject @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        Page<OrderResponse> page = orderService.listCustomerOrders(authentication.getName(), pageable);
        return new PagedModel<>(page);
    }

    @Operation(summary = "Detalle de mi pedido", description = "Devuelve un pedido propio")
    @ApiResponse(responseCode = "200", description = "Pedido encontrado")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "No tiene el rol requerido")
    @ApiResponse(responseCode = "404", description = "El pedido no existe")
    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Long id, Authentication authentication) {
        return orderService.getCustomerOrder(authentication.getName(), id);
    }
}
