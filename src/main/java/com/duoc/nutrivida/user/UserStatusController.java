package com.duoc.nutrivida.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Usuarios (administración)", description = "Activación y desactivación de cuentas")
public class UserStatusController {

    private final UserStatusService statusService;

    public UserStatusController(UserStatusService statusService) {
        this.statusService = statusService;
    }

    @Operation(summary = "Activa o desactiva un usuario",
            description = "Un admin no puede desactivarse a sí mismo ni dejar al sistema sin un ADMIN activo")
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    @ApiResponse(responseCode = "400", description = "Regla de negocio incumplida o datos inválidos")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "No tiene rol ADMIN")
    @ApiResponse(responseCode = "404", description = "Usuario no existe")
    @PatchMapping("/{id}/status")
    public ResponseEntity<UserAdminResponse> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(statusService.changeStatus(id, request.enabled(), authentication.getName()));
    }
}