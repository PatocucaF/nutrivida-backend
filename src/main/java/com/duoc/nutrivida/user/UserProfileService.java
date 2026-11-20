package com.duoc.nutrivida.user;

import com.duoc.nutrivida.audit.AuditAction;
import com.duoc.nutrivida.audit.AuditService;
import com.duoc.nutrivida.common.BusinessRuleException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserProfileService(UserService userService, UserRepository userRepository,
                              PasswordEncoder passwordEncoder, AuditService auditService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional
    public UserAdminResponse updateProfile(String email, ProfileUpdateRequest request) {
        User user = userService.findByEmail(email);
        user.setName(request.name().trim());
        userRepository.save(user);
        auditService.register(AuditAction.PERFIL_EDITADO, user.getId(), "El usuario actualizó su nombre");
        return UserAdminResponse.from(user);
    }

    @Transactional
    public void changePassword(String email, PasswordChangeRequest request) {
        User user = userService.findByEmail(email);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessRuleException("La contraseña actual es incorrecta");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BusinessRuleException("La nueva contraseña debe ser distinta de la actual");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        auditService.register(AuditAction.CONTRASENA_CAMBIADA, user.getId(), "El usuario cambió su contraseña");
    }
}