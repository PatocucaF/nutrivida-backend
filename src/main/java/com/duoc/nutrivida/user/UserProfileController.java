package com.duoc.nutrivida.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me")
@PreAuthorize("isAuthenticated()")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Mi perfil", description = "Edición del perfil propio. Cualquier usuario autenticado")
public class UserProfileController {

    private final UserProfileService profileService;

    public UserProfileController(UserProfileService profileService) {
        this.profileService = profileService;
    }

    @Operation(summary = "Edita mi nombre")
    @ApiResponse(responseCode = "200", description = "Perfil actualizado")
    @ApiResponse(responseCode = "400", description = "Nombre inválido")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @PutMapping
    public ResponseEntity<UserAdminResponse> updateProfile(
            @Valid @RequestBody ProfileUpdateRequest request, Authentication authentication) {
        return ResponseEntity.ok(profileService.updateProfile(authentication.getName(), request));
    }

    @Operation(summary = "Cambia mi contraseña",
            description = "Exige la contraseña actual. La nueva debe ser distinta de la actual")
    @ApiResponse(responseCode = "204", description = "Contraseña cambiada")
    @ApiResponse(responseCode = "400", description = "Contraseña actual incorrecta o datos inválidos")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody PasswordChangeRequest request, Authentication authentication) {
        profileService.changePassword(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }
}