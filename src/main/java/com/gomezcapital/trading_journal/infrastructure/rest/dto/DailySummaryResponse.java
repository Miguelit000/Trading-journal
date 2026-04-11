package com.gomezcapital.trading_journal.infrastructure.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailySummaryResponse(
    LocalDate date,
    int tradeCount,
    BigDecimal netPnl,
    boolean isProfitable
) {
}