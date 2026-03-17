package com.gomezcapital.trading_journal.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


public record Trade(

    UUID id,
    UUID accountId,
    UUID strategyId,
    UUID playbookId,
    String asset,
    String direction, // 'LONG o SHORT'
    String status, // 'OPEN, CLOSED, CANCELLED'
    LocalDateTime entryDate,
    LocalDateTime exitDate,
    BigDecimal entryPrice,
    BigDecimal exitPrice,
    BigDecimal positionSize,
    BigDecimal takeProfit,
    BigDecimal stopLoss,
    BigDecimal PlannedRr,
    BigDecimal actualRr,
    BigDecimal mfePrice,
    BigDecimal maePrice,
    BigDecimal commissions,
    BigDecimal feeAndSwaps,
    BigDecimal pnlGross,
    BigDecimal pnlNet,
    String notes

) {
    
}
