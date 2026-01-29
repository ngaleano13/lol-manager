package com.ngaleano.lol_manager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserDTO(
        @NotBlank(message = "El email es requerido") @Email(message = "El email debe ser valido") String email,

        @NotBlank(message = "La contraseña es requerida") @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres") String password,

        @NotBlank(message = "El nickname es requerido") String nickname) {
}
