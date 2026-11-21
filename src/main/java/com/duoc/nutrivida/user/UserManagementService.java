package com.duoc.nutrivida.user;

import com.duoc.nutrivida.audit.AuditAction;
import com.duoc.nutrivida.audit.AuditService;
import com.duoc.nutrivida.common.BusinessRuleException;
import com.duoc.nutrivida.common.ConflictException;
import com.duoc.nutrivida.common.NotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserManagementService(UserRepository userRepository, RoleRepository roleRepository,
                                 UserService userService, PasswordEncoder passwordEncoder,
                                 AuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional
    public UserAdminResponse create(UserCreateRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("El correo ya está registrado");
        }
        Role role = resolveRole(request.role());

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEnabled(true);
        user.setRoles(new HashSet<>(Set.of(role)));

        User saved = userRepository.save(user);
        auditService.register(AuditAction.USUARIO_CREADO, saved.getId(),
                "Correo: " + email + ", rol: " + role.getName());
        return UserAdminResponse.from(saved);
    }

    @Transactional
    public UserAdminResponse update(Long id, UserUpdateRequest request) {
        User user = userService.findById(id);
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ConflictException("El correo ya está registrado por otro usuario");
        }

        Role newRole = resolveRole(request.role());
        boolean wasAdmin = hasRole(user, RoleName.ADMIN);
        boolean roleChanged = !hasRole(user, newRole.getName()) || user.getRoles().size() != 1;

        if (user.isEnabled() && wasAdmin && newRole.getName() != RoleName.ADMIN
                && userRepository.countByRoles_NameAndEnabledTrueAndDeletedAtIsNull(RoleName.ADMIN) <= 1) {
            throw new BusinessRuleException("No se puede quitar el rol al último administrador activo");
        }

        String oldRoles = rolesAsText(user);

        user.setName(request.name().trim());
        user.setEmail(email);
        user.getRoles().clear();
        user.getRoles().add(newRole);
        userRepository.save(user);

        auditService.register(AuditAction.USUARIO_EDITADO, id, "Datos actualizados. Correo: " + email);
        if (roleChanged) {
            auditService.register(AuditAction.ROL_CAMBIADO, id, oldRoles + " -> " + newRole.getName());
        }
        return UserAdminResponse.from(user);
    }

    // Baja logica: la fila NO se borra, para conservar el historial y las referencias de otras tablas
    @Transactional
    public void delete(Long id, String actorEmail) {
        User user = userService.findById(id);

        if (user.getEmail().equalsIgnoreCase(actorEmail)) {
            throw new BusinessRuleException("No puedes eliminar tu propia cuenta");
        }
        if (user.isEnabled() && hasRole(user, RoleName.ADMIN)
                && userRepository.countByRoles_NameAndEnabledTrueAndDeletedAtIsNull(RoleName.ADMIN) <= 1) {
            throw new BusinessRuleException("No se puede eliminar al último administrador activo");
        }

        user.setEnabled(false);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);

        auditService.register(AuditAction.USUARIO_ELIMINADO, id, "Baja lógica de " + user.getEmail());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private Role resolveRole(String roleText) {
        RoleName name;
        try {
            name = RoleName.valueOf(roleText.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleException("Rol inválido. Valores permitidos: ADMIN, VENDEDOR, CLIENTE");
        }
        return roleRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Rol no encontrado: " + name));
    }

    private boolean hasRole(User user, RoleName name) {
        return user.getRoles().stream().anyMatch(r -> r.getName() == name);
    }

    private String rolesAsText(User user) {
        return user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.joining(","));
    }
}