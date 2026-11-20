package com.duoc.nutrivida.user;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
public class UserStatusController {

    private final UserStatusService statusService;

    public UserStatusController(UserStatusService statusService) {
        this.statusService = statusService;
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<UserAdminResponse> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(statusService.changeStatus(id, request.enabled(), authentication.getName()));
    }
}