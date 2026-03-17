package com.gomezcapital.trading_journal.domain.ports;

import com.gomezcapital.trading_journal.domain.model.Trade;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TradeRepositoryPort {
    // Guarda un trade nuevo o actualiza uno existente
    Trade save(Trade trade);

    // Busca un trade especifico por Id
    // Usamos Optional porque podria no existir en la base de datos
    Optional<Trade> findById(UUID id);

    // Trae todos los trades de una cuenta especifica
    List<Trade> findByAccountId(UUID accountId);

    // Elimina un trade
    void deleteById(UUID id);
}
