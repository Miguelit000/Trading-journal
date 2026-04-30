package com.gomezcapital.trading_journal.infrastructure.rest.dto;

import java.util.UUID;

public record CreateStrategyRequest(UUID portfolioId, String name, String description, String rules) {
    
}
