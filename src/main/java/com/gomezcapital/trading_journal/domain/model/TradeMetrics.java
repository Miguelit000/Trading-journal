package com.gomezcapital.trading_journal.domain.model;

import java.math.BigDecimal;

// Record para almacenar los calculos matematicos del dasboard

public record TradeMetrics(
    int totalTrades,
    int winningTrades,
    int losingTrades,
    BigDecimal winRate,
    BigDecimal profitFactor,
    BigDecimal totalPnl

) {

}
