package com.gomezcapital.trading_journal.infrastructure.rest.dto;

import java.math.BigDecimal;

public record UpdateTargetRequest(
    BigDecimal targetBalance
) {
}