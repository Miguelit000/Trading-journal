package com.gomezcapital.trading_journal.infrastructure.rest.controller;

import com.gomezcapital.trading_journal.application.service.SecurityAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.gomezcapital.trading_journal.domain.ports.UserRepositoryPort;
import com.gomezcapital.trading_journal.domain.model.User;
import java.util.List;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final SecurityAuditService securityAuditService;
    private final UserRepositoryPort userRepositoryPort;

    // --- PANEL DE CIBERSEGURIDAD ---

    @GetMapping("/security/logs")
    public ResponseEntity<?> getSecurityLogs() {
        return ResponseEntity.ok(securityAuditService.getRecentLogs());
    }

    @GetMapping("/security/blocked-ips")
    public ResponseEntity<?> getBlockedIps() {
        return ResponseEntity.ok(securityAuditService.getBlockedIps());
    }

    @PostMapping("/security/block-ip")
    public ResponseEntity<?> blockIp(@RequestBody Map<String, String> request) {
        String ip = request.get("ipAddress");
        String reason = request.getOrDefault("reason", "Bloqueo manual desde el SOC");
        securityAuditService.blockIp(ip, reason);
        return ResponseEntity.ok(Map.of("message", "IP " + ip + " bloqueada exitosamente"));
    }

    @DeleteMapping("/security/unblock-ip/{ip}")
    public ResponseEntity<?> unblockIp(@PathVariable String ip) {
        securityAuditService.unblockIp(ip);
        return ResponseEntity.ok(Map.of("message", "IP " + ip + " desbloqueada"));
    }

    @GetMapping("/metrics")
    public ResponseEntity<?> getBusinessMetrics() {
        List<User> allUsers = userRepositoryPort.findAll();
        long totalUsers = allUsers.size();
        long proUsers = allUsers.stream().filter(u -> "ROLE_PRO".equals(u.role())).count();
        
        return ResponseEntity.ok(Map.of(
            "totalUsers", totalUsers,
            "proUsers", proUsers,
            "freeUsers", totalUsers - proUsers
        ));
    }
}