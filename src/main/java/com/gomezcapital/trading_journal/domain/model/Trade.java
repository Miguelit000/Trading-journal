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
    String direction,
    String status,
    LocalDateTime entryDate,
    LocalDateTime exitDate,
    BigDecimal entryPrice,
    BigDecimal exitPrice,
    BigDecimal positionSize,
    BigDecimal takeProfit,
    BigDecimal stopLoss,
    BigDecimal plannedRr,
    BigDecimal actualRr,
    BigDecimal mfePrice,
    BigDecimal maePrice,
    BigDecimal commissions,
    BigDecimal feesAndSwaps,
    BigDecimal pnlGross,
    BigDecimal pnlNet,
    String notes
) {
}