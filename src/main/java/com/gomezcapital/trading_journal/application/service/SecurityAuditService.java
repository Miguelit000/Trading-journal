package com.gomezcapital.trading_journal.application.service;

import com.gomezcapital.trading_journal.infrastructure.persistence.entity.BlockedIpEntity;
import com.gomezcapital.trading_journal.infrastructure.persistence.entity.SecurityLogEntity;
import com.gomezcapital.trading_journal.infrastructure.persistence.repository.BlockedIpRepository;
import com.gomezcapital.trading_journal.infrastructure.persistence.repository.SecurityLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityAuditService {

    private final SecurityLogRepository securityLogRepository;
    private final BlockedIpRepository blockedIpRepository;

    public void logEvent(String ipAddress, String eventType, String severity, String userEmail, String details) {
        SecurityLogEntity logEntity = SecurityLogEntity.builder()
                .ipAddress(ipAddress != null ? ipAddress : "UNKNOWN")
                .eventType(eventType)
                .severity(severity)
                .userEmail(userEmail)
                .details(details)
                .build();
        securityLogRepository.save(logEntity);
        log.info("🛡️ [SOC] {} - IP: {} - Gravedad: {}", eventType, ipAddress, severity);
    }

    public boolean isIpBlocked(String ipAddress) {
        if (ipAddress == null) return false;
        return blockedIpRepository.existsByIpAddress(ipAddress);
    }

    @Transactional
    public void blockIp(String ipAddress, String reason) {
        if (!isIpBlocked(ipAddress)) {
            BlockedIpEntity blockedIp = BlockedIpEntity.builder()
                    .ipAddress(ipAddress)
                    .reason(reason)
                    .build();
            blockedIpRepository.save(blockedIp);
            log.warn("🚫 [SOC] IP enviada a la lista negra: {} - Motivo: {}", ipAddress, reason);
        }
    }

    @Transactional
    public void unblockIp(String ipAddress) {
        blockedIpRepository.deleteByIpAddress(ipAddress);
        log.info("✅ [SOC] IP Desbloqueada: {}", ipAddress);
    }

    public List<SecurityLogEntity> getRecentLogs() {
        return securityLogRepository.findTop100ByOrderByTimestampDesc();
    }

    public List<BlockedIpEntity> getBlockedIps() {
        return blockedIpRepository.findAll();
    }
}