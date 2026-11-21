package com.duoc.nutrivida.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 80, message = "El nombre debe tener entre 2 y 80 caracteres")
        @Pattern(regexp = "^[\\p{L}][\\p{L} .'-]*$",
                message = "El nombre solo puede tener letras, espacios, punto, apóstrofe y guion")
        String name,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Debe ingresar un email válido")
        @Size(max = 120, message = "El email no puede superar 120 caracteres")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, max = 72, message = "La contraseña debe tener entre 6 y 72 caracteres")
        String password,

        @NotBlank(message = "El rol es obligatorio")
        String role
) {}