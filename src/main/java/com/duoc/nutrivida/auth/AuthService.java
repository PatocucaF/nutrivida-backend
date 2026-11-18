package com.duoc.nutrivida.auth;

import com.duoc.nutrivida.common.ConflictException;
import com.duoc.nutrivida.common.NotFoundException;
import com.duoc.nutrivida.security.JwtService;
import com.duoc.nutrivida.user.Role;
import com.duoc.nutrivida.user.RoleName;
import com.duoc.nutrivida.user.RoleRepository;
import com.duoc.nutrivida.user.User;
import com.duoc.nutrivida.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("El correo ya está registrado");
        }

        // El registro publico SIEMPRE crea un CLIENTE. Cambiar de rol es cosa del ADMIN.
        Role role = roleRepository.findByName(RoleName.CLIENTE)
                .orElseThrow(() -> new NotFoundException("Rol CLIENTE no encontrado"));

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        user.setRoles(new HashSet<>(Set.of(role)));

        User saved = userRepository.save(user);
        return buildResponse(saved, jwtService.generateToken(saved.getEmail(), authorities(saved)));
    }

    public AuthResponse login(AuthRequest request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        return buildResponse(user, jwtService.generateToken(user.getEmail(), authorities(user)));
    }

    private List<String> authorities(User user) {
        return user.getRoles().stream().map(r -> "ROLE_" + r.getName().name()).toList();
    }

    private AuthResponse buildResponse(User user, String token) {
        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toSet())
        );
        return new AuthResponse(token, "Bearer", userResponse);
    }
}