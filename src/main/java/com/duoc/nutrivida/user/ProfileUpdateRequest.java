package com.duoc.nutrivida.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 80, message = "El nombre debe tener entre 2 y 80 caracteres")
        @Pattern(regexp = "^[\\p{L}][\\p{L} .'-]*$",
                message = "El nombre solo puede tener letras, espacios, punto, apóstrofe y guion")
        String name
) {}