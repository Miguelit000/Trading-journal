package com.gomezcapital.trading_journal.infrastructure.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es valido")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    String password
) {
    
}
