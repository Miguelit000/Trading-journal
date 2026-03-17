package com.gomezcapital.trading_journal.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record Account(

    UUID id,
    UUID userId,
    String name,
    BigDecimal initialBalance,
    BigDecimal currentBalance,
    String currency,
    LocalDateTime createdAt

) {
    
}