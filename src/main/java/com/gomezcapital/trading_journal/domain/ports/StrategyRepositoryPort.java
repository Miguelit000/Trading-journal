package com.gomezcapital.trading_journal.domain.ports;

import com.gomezcapital.trading_journal.domain.model.Strategy;
import java.util.List;
import java.util.UUID;


public interface StrategyRepositoryPort {

    Strategy save(Strategy strategy);
    List<Strategy> findByPortfolioId(UUID portfolioId);
    
}
