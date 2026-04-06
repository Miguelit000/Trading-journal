package com.gomezcapital.trading_journal.domain.model;

import java.util.UUID;

public record User(

    UUID id,
    String email,
    String passwordHash,
    String alias
) {
    
}
