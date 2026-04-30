package com.gomezcapital.trading_journal.infrastructure.rest.dto;

import java.math.BigDecimal;

public record UpdatePortfolioBalancesRequest(
    BigDecimal initialBalance,
    BigDecimal currentBalance,
    BigDecimal targetBalance
) {
}