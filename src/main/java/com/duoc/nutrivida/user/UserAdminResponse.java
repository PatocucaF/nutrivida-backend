package com.duoc.nutrivida.user;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record UserAdminResponse(
        Long id,
        String name,
        String email,
        boolean enabled,
        Set<String> roles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserAdminResponse from(User u) {
        return new UserAdminResponse(
                u.getId(),
                u.getName(),
                u.getEmail(),
                u.isEnabled(),
                u.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet()),
                u.getCreatedAt(),
                u.getUpdatedAt()
        );
    }
}