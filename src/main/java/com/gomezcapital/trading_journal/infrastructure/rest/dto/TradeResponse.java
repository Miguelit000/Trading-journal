package com.gomezcapital.trading_journal.infrastructure.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public record TradeResponse( 
    UUID id,
    String asset,
    String direction,
    String status,
    LocalDateTime entryDate,
    BigDecimal entryPrice,
    BigDecimal positionSize,
    BigDecimal pnlNet,
    String notes, 
    List<String> images
) {
}