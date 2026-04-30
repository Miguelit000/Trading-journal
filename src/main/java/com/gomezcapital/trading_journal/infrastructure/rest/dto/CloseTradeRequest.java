package com.gomezcapital.trading_journal.infrastructure.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Solo le pedimos al frontend los datos estrictamente necesarios para cerrar la operación.
public record CloseTradeRequest(
    BigDecimal exitPrice,
    LocalDateTime exitDate,
    BigDecimal pnlNet // si el frontend no lo envía, usaremos la hora actual del servidor.
) {
}