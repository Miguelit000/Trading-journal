package com.gomezcapital.trading_journal.infrastructure.rest.dto;

// Solo devolvemos el token, el frontend lo guardara en el localstorage o cookies
public record AuthenticationResponse(
    String token
) {
    
}