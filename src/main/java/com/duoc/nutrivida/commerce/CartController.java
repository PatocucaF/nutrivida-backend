package com.duoc.nutrivida.commerce;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Carrito", description = "Carrito de compras del cliente. Requiere rol CLIENTE")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @Operation(summary = "Ver carrito", description = "Devuelve el carrito del cliente autenticado")
    @ApiResponse(responseCode = "200", description = "Carrito entregado")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "No tiene el rol requerido")
    @GetMapping
    public CartResponse get(Authentication authentication) {
        return cartService.get(authentication.getName());
    }

    @Operation(summary = "Agrega un producto", description = "Agrega el producto al carrito con la cantidad indicada")
    @ApiResponse(responseCode = "200", description = "Carrito actualizado")
    @ApiResponse(responseCode = "400", description = "Producto no disponible o cantidad supera el stock")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "No tiene el rol requerido")
    @ApiResponse(responseCode = "404", description = "El producto no existe")
    @PostMapping("/items/{productId}")
    public CartResponse add(@PathVariable Long productId, @Valid @RequestBody CartItemRequest request,
                            Authentication authentication) {
        return cartService.add(authentication.getName(), productId, request.quantity());
    }

    @Operation(summary = "Cambia la cantidad", description = "Fija la cantidad del producto en el carrito")
    @ApiResponse(responseCode = "200", description = "Carrito actualizado")
    @ApiResponse(responseCode = "400", description = "Producto no disponible o cantidad supera el stock")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "No tiene el rol requerido")
    @ApiResponse(responseCode = "404", description = "El producto no existe")
    @PutMapping("/items/{productId}")
    public CartResponse update(@PathVariable Long productId, @Valid @RequestBody CartItemRequest request,
                               Authentication authentication) {
        return cartService.add(authentication.getName(), productId, request.quantity());
    }

    @Operation(summary = "Quita un producto", description = "Elimina el producto del carrito")
    @ApiResponse(responseCode = "200", description = "Carrito actualizado")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "No tiene el rol requerido")
    @ApiResponse(responseCode = "404", description = "El producto no está en el carrito")
    @DeleteMapping("/items/{productId}")
    public CartResponse remove(@PathVariable Long productId, Authentication authentication) {
        return cartService.remove(authentication.getName(), productId);
    }

    @Operation(summary = "Vacía el carrito", description = "Elimina todos los productos del carrito")
    @ApiResponse(responseCode = "200", description = "Carrito vaciado")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "No tiene el rol requerido")
    @DeleteMapping
    public void clear(Authentication authentication) {
        cartService.clear(authentication.getName());
    }
}
