package com.gomezcapital.trading_journal.domain.model;

import java.util.UUID;

public record Strategy(

    UUID id,
    UUID portfolioId,
    String name,
    String description,
    String rules
) {
    
}
