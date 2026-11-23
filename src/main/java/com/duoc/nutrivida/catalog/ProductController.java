package com.duoc.nutrivida.catalog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Productos", description = "Catálogo de productos. Lectura para cualquier usuario autenticado; crear y editar requiere ADMIN")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "Lista productos", description = "Devuelve los productos activos, con filtro opcional por categoría. includeInactive=true (solo ADMIN) incluye los inactivos")
    @ApiResponse(responseCode = "200", description = "Listado entregado")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "No tiene el rol requerido")
    @GetMapping
    @PreAuthorize("!#includeInactive or hasRole('ADMIN')")
    public List<ProductResponse> list(@RequestParam(required = false) Long categoryId,
                                      @RequestParam(defaultValue = "false") boolean includeInactive) {
        return productService.list(categoryId, includeInactive);
    }

    @Operation(summary = "Detalle de un producto", description = "Devuelve un producto activo por su id")
    @ApiResponse(responseCode = "200", description = "Producto encontrado")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "404", description = "Producto no existe o está inactivo")
    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Long id) {
        return productService.get(id);
    }

    @Operation(summary = "Crea un producto", description = "Requiere ADMIN. Registra el stock inicial como movimiento de inventario")
    @ApiResponse(responseCode = "201", description = "Producto creado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "No tiene el rol requerido")
    @ApiResponse(responseCode = "404", description = "La categoría no existe")
    @ApiResponse(responseCode = "409", description = "Ya existe un producto con ese SKU")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse create(@Valid @RequestBody ProductRequest request, Authentication authentication) {
        return productService.create(request, authentication.getName());
    }

    @Operation(summary = "Actualiza un producto", description = "Requiere ADMIN. No modifica el stock (se ajusta desde inventario)")
    @ApiResponse(responseCode = "200", description = "Producto actualizado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "No tiene el rol requerido")
    @ApiResponse(responseCode = "404", description = "Producto o categoría no existe")
    @ApiResponse(responseCode = "409", description = "Ya existe un producto con ese SKU")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductUpdateRequest request) {
        return productService.update(id, request);
    }
}
