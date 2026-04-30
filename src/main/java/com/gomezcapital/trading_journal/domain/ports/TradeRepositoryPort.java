package com.gomezcapital.trading_journal.domain.ports;

import com.gomezcapital.trading_journal.domain.model.Trade;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TradeRepositoryPort {
   
    Trade save(Trade trade);
    Optional<Trade> findById(UUID id);
    List<Trade> findByPortfolioId(UUID portfolioId);
    void deleteById(UUID id);
}
