package com.gomezcapital.trading_journal.infrastructure.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

// Solo devolvemos la informacion que necesita en la pantalla
public record TradeResponse( 
    
    UUID id,
    String asset,
    String direction,
    String status,
    LocalDateTime entryDate,
    BigDecimal entryPrice,
    BigDecimal positionSize,
    BigDecimal pnlNet,
    String imageName
) {
    
}