package com.gomezcapital.trading_journal.infrastructure.persistence.repository;

import com.gomezcapital.trading_journal.infrastructure.persistence.entity.TradeImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository

public interface TradeImageJpaRepository extends JpaRepository<TradeImageEntity, UUID> {
    List<TradeImageEntity> findByTradeId(UUID tradeId);
    
}
