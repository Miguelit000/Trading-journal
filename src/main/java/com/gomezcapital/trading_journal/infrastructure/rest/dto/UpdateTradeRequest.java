package com.gomezcapital.trading_journal.infrastructure.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateTradeRequest(

    UUID strategyId,
    String asset,
    String direction,
    BigDecimal entryPrice,
    BigDecimal positionSize,
    BigDecimal takeProfit,
    BigDecimal stopLoss,
    BigDecimal exitPrice,
    BigDecimal pnlNet,
    String notes
) {

}
