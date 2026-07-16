package com.gomezcapital.trading_journal.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "security_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "event_type", nullable = false)
    private String eventType; // Ej: SPOOFING_ATTEMPT, PRIVILEGE_ESCALATION

    @Column(name = "severity", nullable = false)
    private String severity; // Ej: INFO, WARNING, CRITICAL

    @Column(name = "user_email")
    private String userEmail; // Para saber qué cuenta intentaron vulnerar (si aplica)

    @Column(name = "details", length = 500)
    private String details;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}