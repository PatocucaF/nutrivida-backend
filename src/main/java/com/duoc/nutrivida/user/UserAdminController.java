package com.duoc.nutrivida.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Usuarios (administración)", description = "Consulta de usuarios. Requiere rol ADMIN")
public class UserAdminController {

    private final UserQueryService queryService;

    public UserAdminController(UserQueryService queryService) {
        this.queryService = queryService;
    }

    @Operation(summary = "Lista usuarios",
            description = "Listado paginado. Se puede filtrar por estado (enabled). Excluye usuarios dados de baja")
    @ApiResponse(responseCode = "200", description = "Listado entregado")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "No tiene rol ADMIN")
    @GetMapping
    public ResponseEntity<PagedModel<UserAdminResponse>> list(
            @RequestParam(required = false) Boolean enabled,
            @ParameterObject @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        Page<UserAdminResponse> page = queryService.list(enabled, pageable);
        return ResponseEntity.ok(new PagedModel<>(page));
    }

    @Operation(summary = "Detalle de un usuario", description = "No expone la contraseña")
    @ApiResponse(responseCode = "200", description = "Usuario encontrado")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "No tiene rol ADMIN")
    @ApiResponse(responseCode = "404", description = "Usuario no existe o fue dado de baja")
    @GetMapping("/{id}")
    public ResponseEntity<UserAdminResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(queryService.getById(id));
    }
}