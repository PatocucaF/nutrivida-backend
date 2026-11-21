package com.duoc.nutrivida.user;

import com.duoc.nutrivida.audit.AuditAction;
import com.duoc.nutrivida.audit.AuditService;
import com.duoc.nutrivida.common.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock private UserService userService;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuditService auditService;
    @InjectMocks private UserProfileService service;

    private User userWithPassword() {
        User u = new User();
        u.setId(5L);
        u.setEmail("a@x.cl");
        u.setPassword("hash");
        return u;
    }

    @Test
    void rechazaCambioSiLaContrasenaActualEsIncorrecta() {
        when(userService.findByEmail("a@x.cl")).thenReturn(userWithPassword());
        when(passwordEncoder.matches("mala", "hash")).thenReturn(false);

        assertThrows(BusinessRuleException.class,
                () -> service.changePassword("a@x.cl", new PasswordChangeRequest("mala", "nueva1")));
        verify(userRepository, never()).save(any());
    }

    @Test
    void rechazaUnaNuevaContrasenaIgualALaActual() {
        when(userService.findByEmail("a@x.cl")).thenReturn(userWithPassword());
        when(passwordEncoder.matches("vieja", "hash")).thenReturn(true);
        when(passwordEncoder.matches("nueva1", "hash")).thenReturn(true);

        assertThrows(BusinessRuleException.class,
                () -> service.changePassword("a@x.cl", new PasswordChangeRequest("vieja", "nueva1")));
    }

    @Test
    void cambiaLaContrasenaYDejaAuditoria() {
        User user = userWithPassword();
        when(userService.findByEmail("a@x.cl")).thenReturn(user);
        when(passwordEncoder.matches("vieja", "hash")).thenReturn(true);
        when(passwordEncoder.matches("nueva1", "hash")).thenReturn(false);
        when(passwordEncoder.encode("nueva1")).thenReturn("nuevoHash");

        service.changePassword("a@x.cl", new PasswordChangeRequest("vieja", "nueva1"));

        assertEquals("nuevoHash", user.getPassword());
        verify(userRepository).save(user);
        verify(auditService).register(eq(AuditAction.CONTRASENA_CAMBIADA), eq(5L), anyString());
    }
}