package com.gomezcapital.trading_journal.infrastructure.rest.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse (

    int status,
    String message,
    List<String> details,
    LocalDateTime timestamp
) {
    
}
