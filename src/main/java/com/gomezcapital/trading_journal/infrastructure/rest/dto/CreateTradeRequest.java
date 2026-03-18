package com.gomezcapital.trading_journal.infrastructure.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

// Solo se pide lo esencial para abrir un trade
public record CreateTradeRequest(
    UUID accountId,
    UUID strategyId,
    UUID playbookId,
    String asset,
    String direction, // 'LONG O SHORT'
    BigDecimal entryPrice,
    BigDecimal positionSize,
    BigDecimal takeProfit,
    BigDecimal stopLoss,
    String notes 
) {
    
}