package com.duoc.nutrivida.audit;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        String actorEmail,
        AuditAction action,
        Long targetUserId,
        String detail,
        LocalDateTime createdAt
) {
    public static AuditLogResponse from(AuditLog a) {
        return new AuditLogResponse(a.getId(), a.getActorEmail(), a.getAction(),
                a.getTargetUserId(), a.getDetail(), a.getCreatedAt());
    }
}