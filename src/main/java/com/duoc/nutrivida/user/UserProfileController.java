package com.duoc.nutrivida.user;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
public class UserProfileController {

    private final UserProfileService profileService;

    public UserProfileController(UserProfileService profileService) {
        this.profileService = profileService;
    }

    @PutMapping
    public ResponseEntity<UserAdminResponse> updateProfile(
            @Valid @RequestBody ProfileUpdateRequest request, Authentication authentication) {
        return ResponseEntity.ok(profileService.updateProfile(authentication.getName(), request));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody PasswordChangeRequest request, Authentication authentication) {
        profileService.changePassword(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }
}