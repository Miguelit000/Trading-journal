package com.gomezcapital.trading_journal.infrastructure.persistence.repository;

import com.gomezcapital.trading_journal.infrastructure.persistence.entity.PortfolioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository

public interface PortfolioJpaRepository extends JpaRepository<PortfolioEntity, UUID> {
    
    List<PortfolioEntity> findByUserId(UUID userId);
}

