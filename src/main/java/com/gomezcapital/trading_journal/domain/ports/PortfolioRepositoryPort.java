package com.gomezcapital.trading_journal.domain.ports;

import com.gomezcapital.trading_journal.domain.model.Portfolio; 
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioRepositoryPort {

    Portfolio save(Portfolio portfolio);
    Optional<Portfolio> findById(UUID id);
    List<Portfolio> findByUserId(UUID userId);
    void deleteById(UUID id);


}