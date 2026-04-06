package com.gomezcapital.trading_journal.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record TradeImage(

    UUID id,
    UUID tradeId,
    String fileName,
    LocalDateTime createdAt
){
    
}
