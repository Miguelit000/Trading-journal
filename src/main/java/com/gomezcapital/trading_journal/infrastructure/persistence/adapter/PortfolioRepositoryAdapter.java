package com.gomezcapital.trading_journal.infrastructure.persistence.adapter;

import com.gomezcapital.trading_journal.domain.model.Portfolio;
import com.gomezcapital.trading_journal.domain.ports.PortfolioRepositoryPort;
import com.gomezcapital.trading_journal.infrastructure.persistence.entity.PortfolioEntity;
import com.gomezcapital.trading_journal.infrastructure.persistence.repository.PortfolioJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component 
@RequiredArgsConstructor
public class PortfolioRepositoryAdapter implements PortfolioRepositoryPort {

    private final PortfolioJpaRepository portfolioJpaRepository;

    @Override
    public Portfolio save(Portfolio portfolio) {
        PortfolioEntity entity = toEntity(portfolio);
        PortfolioEntity savedEntity = portfolioJpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Portfolio> findById(UUID id) {
        return portfolioJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Portfolio> findByUserId(UUID userId) {
        return portfolioJpaRepository.findByUserId(userId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id) {
        portfolioJpaRepository.deleteById(id);
    }

    private PortfolioEntity toEntity(Portfolio portfolio) {
        if (portfolio == null) return null;
        return PortfolioEntity.builder()
                .id(portfolio.id())
                .userId(portfolio.userId())
                .name(portfolio.name())
                .initialBalance(portfolio.initialBalance())
                .currentBalance(portfolio.currentBalance())
                .targetBalance(portfolio.targetBalance()) // <-- NUEVO CAMPO AÑADIDO
                .currency(portfolio.currency())
                .createdAt(portfolio.createdAt())
                .build();
    }

    private Portfolio toDomain(PortfolioEntity entity) {
        if (entity == null) return null;
        return new Portfolio(
                entity.getId(),
                entity.getUserId(),
                entity.getName(),
                entity.getInitialBalance(),
                entity.getCurrentBalance(),
                entity.getTargetBalance(), // <-- NUEVO CAMPO AÑADIDO
                entity.getCurrency(),
                entity.getCreatedAt()
        );
    }
}