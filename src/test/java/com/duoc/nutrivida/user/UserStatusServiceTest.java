package com.duoc.nutrivida.user;

import com.duoc.nutrivida.audit.AuditAction;
import com.duoc.nutrivida.audit.AuditService;
import com.duoc.nutrivida.common.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserStatusServiceTest {

    @Mock private UserService userService;
    @Mock private UserRepository userRepository;
    @Mock private AuditService auditService;
    @InjectMocks private UserStatusService service;

    private User newUser(Long id, String email, boolean enabled, RoleName role) {
        User u = new User();
        u.setId(id);
        u.setName("Test");
        u.setEmail(email);
        u.setEnabled(enabled);
        u.setRoles(new HashSet<>(Set.of(new Role(1L, role))));
        return u;
    }

    @Test
    void unAdminNoPuedeDesactivarSuPropiaCuenta() {
        when(userService.findById(1L)).thenReturn(newUser(1L, "admin@x.cl", true, RoleName.ADMIN));

        assertThrows(BusinessRuleException.class, () -> service.changeStatus(1L, false, "ADMIN@x.cl"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void desactivaUnUsuarioYDejaAuditoria() {
        User cliente = newUser(2L, "cliente@x.cl", true, RoleName.CLIENTE);
        when(userService.findById(2L)).thenReturn(cliente);

        service.changeStatus(2L, false, "admin@x.cl");

        assertFalse(cliente.isEnabled());
        verify(auditService).register(eq(AuditAction.USUARIO_DESACTIVADO), eq(2L), anyString());
    }

    @Test
    void noDesactivaAlUltimoAdministradorActivo() {
        when(userService.findById(3L)).thenReturn(newUser(3L, "otro@x.cl", true, RoleName.ADMIN));
        when(userRepository.countByRoles_NameAndEnabledTrueAndDeletedAtIsNull(RoleName.ADMIN)).thenReturn(1L);

        assertThrows(BusinessRuleException.class, () -> service.changeStatus(3L, false, "admin@x.cl"));
    }

    @Test
    void rechazaCambiarAlMismoEstado() {
        when(userService.findById(2L)).thenReturn(newUser(2L, "c@x.cl", true, RoleName.CLIENTE));

        assertThrows(BusinessRuleException.class, () -> service.changeStatus(2L, true, "admin@x.cl"));
    }
}