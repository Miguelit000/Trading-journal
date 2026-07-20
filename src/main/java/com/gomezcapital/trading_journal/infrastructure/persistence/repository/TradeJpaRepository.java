package com.gomezcapital.trading_journal.infrastructure.persistence.repository;

import com.gomezcapital.trading_journal.infrastructure.persistence.entity.TradeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TradeJpaRepository extends JpaRepository<TradeEntity, UUID> {
    List<TradeEntity> findByPortfolioId(UUID portfolioId);
    
    Page<TradeEntity> findByPortfolioId(UUID portfolioId, Pageable pageable);
}