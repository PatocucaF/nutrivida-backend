package com.duoc.nutrivida.user;

import com.duoc.nutrivida.audit.AuditAction;
import com.duoc.nutrivida.audit.AuditService;
import com.duoc.nutrivida.common.BusinessRuleException;
import com.duoc.nutrivida.common.ConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UserService userService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuditService auditService;
    @InjectMocks private UserManagementService service;

    @Test
    void noCreaUsuarioConCorreoDuplicadoSinImportarMayusculas() {
        when(userRepository.existsByEmailIgnoreCase("ana@x.cl")).thenReturn(true);
        UserCreateRequest request = new UserCreateRequest("Ana", "  ANA@x.cl ", "123456", "CLIENTE");

        assertThrows(ConflictException.class, () -> service.create(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void rechazaUnRolInexistente() {
        UserCreateRequest request = new UserCreateRequest("Ana", "ana@x.cl", "123456", "SUPERMAN");

        assertThrows(BusinessRuleException.class, () -> service.create(request));
    }

    @Test
    void creaUsuarioConCorreoEnMinusculasYClaveCifrada() {
        when(roleRepository.findByName(RoleName.CLIENTE)).thenReturn(Optional.of(new Role(3L, RoleName.CLIENTE)));
        when(passwordEncoder.encode("123456")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserAdminResponse response =
                service.create(new UserCreateRequest("Ana", "ANA@X.cl", "123456", "cliente"));

        assertEquals("ana@x.cl", response.email());
        verify(passwordEncoder).encode("123456");
        verify(auditService).register(eq(AuditAction.USUARIO_CREADO), any(), anyString());
    }

    @Test
    void laBajaEsLogicaYNoBorraLaFila() {
        User cliente = new User();
        cliente.setId(2L);
        cliente.setEmail("cliente@x.cl");
        cliente.setEnabled(true);
        cliente.setRoles(new HashSet<>(Set.of(new Role(3L, RoleName.CLIENTE))));
        when(userService.findById(2L)).thenReturn(cliente);

        service.delete(2L, "admin@x.cl");

        assertNotNull(cliente.getDeletedAt());
        assertFalse(cliente.isEnabled());
        verify(userRepository, never()).delete(any(User.class));
        verify(userRepository).save(cliente);
    }

    @Test
    void unAdminNoPuedeEliminarseASiMismo() {
        User admin = new User();
        admin.setId(1L);
        admin.setEmail("admin@x.cl");
        admin.setRoles(new HashSet<>(Set.of(new Role(1L, RoleName.ADMIN))));
        when(userService.findById(1L)).thenReturn(admin);

        assertThrows(BusinessRuleException.class, () -> service.delete(1L, "admin@x.cl"));
    }
}