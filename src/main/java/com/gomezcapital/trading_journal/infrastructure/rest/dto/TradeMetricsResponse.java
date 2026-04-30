package com.gomezcapital.trading_journal.infrastructure.rest.dto;

import java.math.BigDecimal;

public record TradeMetricsResponse(

    int totalTrades,
    int winningtrades,
    int losinfTrades,
    BigDecimal winRate,
    BigDecimal profitFactor,
    BigDecimal totalPnl
) {
    
}