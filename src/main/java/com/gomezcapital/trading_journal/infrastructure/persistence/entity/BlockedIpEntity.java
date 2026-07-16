package com.gomezcapital.trading_journal.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "blocked_ips")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class BlockedIpEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "ip_address", nullable = false, unique = true)
    private String ipAddress;

    @Column(name = "reason")
    private String reason; // EJ: "Ataque manual bloqueado por admin"

    @Column(name = "blocked_at", nullable = false, updatable = false)
    private LocalDateTime blockedAt;

    @PrePersist
    protected void onCreate() {
        if (blockedAt == null) {
            blockedAt = LocalDateTime.now();
        }
    }
    
}
