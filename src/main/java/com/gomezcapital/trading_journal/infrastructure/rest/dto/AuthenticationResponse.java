package com.gomezcapital.trading_journal.infrastructure.rest.dto;

import java.util.UUID;


public record AuthenticationResponse(
    String token,
    UUID accountId
) {
    
}