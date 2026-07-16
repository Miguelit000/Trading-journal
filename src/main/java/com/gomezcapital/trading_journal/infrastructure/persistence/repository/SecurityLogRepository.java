package com.gomezcapital.trading_journal.infrastructure.persistence.repository;

import com.gomezcapital.trading_journal.infrastructure.persistence.entity.SecurityLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SecurityLogRepository extends JpaRepository<SecurityLogEntity, UUID> {
    // Nos servirá para el panel frontal: Traer los últimos 100 ataques ordenados por fecha
    List<SecurityLogEntity> findTop100ByOrderByTimestampDesc();
}