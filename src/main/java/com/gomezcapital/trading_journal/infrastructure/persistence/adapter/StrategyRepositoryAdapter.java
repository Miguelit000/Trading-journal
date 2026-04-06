package com.gomezcapital.trading_journal.infrastructure.persistence.adapter;

import com.gomezcapital.trading_journal.domain.model.Strategy;
import com.gomezcapital.trading_journal.domain.ports.StrategyRepositoryPort;
import com.gomezcapital.trading_journal.infrastructure.persistence.entity.StrategyEntity;
import com.gomezcapital.trading_journal.infrastructure.persistence.repository.StrategyJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor

public class StrategyRepositoryAdapter implements StrategyRepositoryPort {

    private final StrategyJpaRepository repository;

    @Override
    public Strategy save(Strategy strategy) {
        StrategyEntity entity = StrategyEntity.builder()
                .id(strategy.id())
                .portfolioId(strategy.portfolioId())
                .name(strategy.name())
                .description(strategy.description())
                .rules(strategy.rules())
                .build();

        StrategyEntity savedEntity = repository.save(entity);

        return new Strategy(
            savedEntity.getId(), savedEntity.getPortfolioId(),
            savedEntity.getName(), savedEntity.getDescription(), savedEntity.getRules()
        );
    }

    @Override
    public List<Strategy> findByPortfolioId(UUID portfolioId) {
        return repository.findByPortfolioId(portfolioId).stream()
                .map(entity -> new Strategy(
                    entity.getId(), entity.getPortfolioId(),
                    entity.getName(), entity.getDescription(),entity.getRules()
                )).toList();
    }
    
}
