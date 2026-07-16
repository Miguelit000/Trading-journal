package com.gomezcapital.trading_journal.infrastructure.persistence.repository;

import com.gomezcapital.trading_journal.infrastructure.persistence.entity.BlockedIpEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlockedIpRepository extends JpaRepository<BlockedIpEntity, UUID> {
    
    // El escudo llamará a este método en cada petición
    boolean existsByIpAddress(String ipAddress);
    
    // Para cuando necesites desbanear a alguien
    Optional<BlockedIpEntity> findByIpAddress(String ipAddress);
    void deleteByIpAddress(String ipAddress);
}