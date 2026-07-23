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
    LocalDateTime exitDate,     
    BigDecimal entryPrice,
    BigDecimal exitPrice,       
    BigDecimal positionSize,
    BigDecimal takeProfit,      
    BigDecimal stopLoss,        
    BigDecimal pnlNet,
    BigDecimal maePrice,        
    BigDecimal mfePrice,        
    UUID strategyId,           
    String notes, 
    List<String> images
) {
}