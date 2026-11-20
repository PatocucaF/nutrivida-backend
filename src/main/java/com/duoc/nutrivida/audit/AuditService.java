package com.duoc.nutrivida.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    // El actor se toma del usuario autenticado en la solicitud actual
    public void register(AuditAction action, Long targetUserId, String detail) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String actor = (auth != null && auth.isAuthenticated()) ? auth.getName() : "sistema";

        AuditLog entry = new AuditLog();
        entry.setActorEmail(actor);
        entry.setAction(action);
        entry.setTargetUserId(targetUserId);
        entry.setDetail(detail);
        auditLogRepository.save(entry);
    }

    public Page<AuditLog> list(Long userId, Pageable pageable) {
        return (userId == null)
                ? auditLogRepository.findAll(pageable)
                : auditLogRepository.findByTargetUserId(userId, pageable);
    }
}