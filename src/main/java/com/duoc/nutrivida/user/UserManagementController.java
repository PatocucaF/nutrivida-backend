package com.duoc.nutrivida.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Usuarios (administración)", description = "Crear, editar y dar de baja usuarios")
public class UserManagementController {

    private final UserManagementService managementService;

    public UserManagementController(UserManagementService managementService) {
        this.managementService = managementService;
    }

    @Operation(summary = "Crea un usuario con rol",
            description = "El correo se guarda en minúsculas y debe ser único. Roles: ADMIN, VENDEDOR, CLIENTE")
    @ApiResponse(responseCode = "201", description = "Usuario creado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o rol inexistente")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "No tiene rol ADMIN")
    @ApiResponse(responseCode = "409", description = "El correo ya está registrado")
    @PostMapping
    public ResponseEntity<UserAdminResponse> create(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(managementService.create(request));
    }

    @Operation(summary = "Edita nombre, correo y rol",
            description = "No permite quitar el rol al último administrador activo")
    @ApiResponse(responseCode = "200", description = "Usuario actualizado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio incumplida")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "No tiene rol ADMIN")
    @ApiResponse(responseCode = "404", description = "Usuario no existe")
    @ApiResponse(responseCode = "409", description = "El correo ya pertenece a otro usuario")
    @PutMapping("/{id}")
    public ResponseEntity<UserAdminResponse> update(
            @PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(managementService.update(id, request));
    }

    @Operation(summary = "Da de baja a un usuario",
            description = "Baja lógica: la fila se conserva para mantener el historial")
    @ApiResponse(responseCode = "204", description = "Usuario dado de baja")
    @ApiResponse(responseCode = "400", description = "No puede eliminarse a sí mismo ni al último ADMIN")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "No tiene rol ADMIN")
    @ApiResponse(responseCode = "404", description = "Usuario no existe")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        managementService.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}