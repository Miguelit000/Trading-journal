package com.gomezcapital.trading_journal.infrastructure.rest.dto;

import java.util.UUID;

public record StrategyResponse( UUID id, UUID accountId, String name, String description, String rules) {

}
