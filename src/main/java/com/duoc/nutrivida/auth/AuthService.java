package com.duoc.nutrivida.auth;

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
        if (userRepository.existsByEmail(request.getEmail().trim())) {
            throw new RuntimeException("El correo ya está registrado");
        }

        RoleName requestedRole = RoleName.valueOf(
                (request.getRole() == null ? "CLIENTE" : request.getRole()).trim().toUpperCase(Locale.ROOT)
        );

        Role role = roleRepository.findByName(requestedRole)
                .orElseGet(() -> roleRepository.findByName(RoleName.CLIENTE)
                        .orElseThrow(() -> new RuntimeException("Rol no encontrado")));

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail().trim().toLowerCase(Locale.ROOT));
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        user.setRoles(Set.of(role));

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(
                savedUser.getEmail(),
                savedUser.getRoles().stream().map(r -> "ROLE_" + r.getName().name()).toList()
        );

        return buildResponse(savedUser, token);
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().trim(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRoles().stream().map(r -> "ROLE_" + r.getName().name()).toList()
        );

        return buildResponse(user, token);
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
