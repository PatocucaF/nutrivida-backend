package com.duoc.nutrivida.user;

import jakarta.validation.constraints.NotNull;

public record UserStatusRequest(
        @NotNull(message = "El campo enabled es obligatorio") Boolean enabled
) {}