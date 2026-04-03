package com.gomezcapital.trading_journal.infrastructure.persistence.repository;

import com.gomezcapital.trading_journal.infrastructure.persistence.entity.StrategyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StrategyJpaRepository extends JpaRepository<StrategyEntity, UUID> {
    List<StrategyEntity> findByAccountId(UUID accountId);
    
}
