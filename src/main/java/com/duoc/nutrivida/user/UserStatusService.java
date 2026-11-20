package com.duoc.nutrivida.user;

import com.duoc.nutrivida.audit.AuditAction;
import com.duoc.nutrivida.audit.AuditService;
import com.duoc.nutrivida.common.BusinessRuleException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserStatusService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public UserStatusService(UserService userService, UserRepository userRepository, AuditService auditService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional
    public UserAdminResponse changeStatus(Long id, boolean enabled, String actorEmail) {
        User user = userService.findById(id);

        if (user.isEnabled() == enabled) {
            throw new BusinessRuleException("El usuario ya está " + (enabled ? "activo" : "desactivado"));
        }

        if (!enabled) {
            if (user.getEmail().equalsIgnoreCase(actorEmail)) {
                throw new BusinessRuleException("No puedes desactivar tu propia cuenta");
            }
            boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ADMIN);
            if (isAdmin && userRepository.countByRoles_NameAndEnabledTrueAndDeletedAtIsNull(RoleName.ADMIN) <= 1) {
                throw new BusinessRuleException("No se puede desactivar al último administrador activo");
            }
        }

        user.setEnabled(enabled);
        userRepository.save(user);

        auditService.register(
                enabled ? AuditAction.USUARIO_ACTIVADO : AuditAction.USUARIO_DESACTIVADO,
                user.getId(),
                "Correo: " + user.getEmail());

        return UserAdminResponse.from(user);
    }
}