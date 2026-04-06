package com.gomezcapital.trading_journal.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "trades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private PortfolioEntity portfolio;

    @Column(name = "strategy_id")
    private UUID strategyId;

    @Column(name = "playbook_id")
    private UUID playbookId;

    @Column(nullable = false, length = 20)
    private String asset;

    @Column(length = 10)
    private String direction;

    @Column(length = 20)
    private String status;

    @Column(name = "entry_date", nullable = false)
    private LocalDateTime entryDate;

    @Column(name = "exit_date")
    private LocalDateTime exitDate;

    @Column(name = "entry_price", nullable = false, precision = 18, scale = 8)
    private BigDecimal entryPrice;

    @Column(name = "exit_price", precision = 18, scale = 8)
    private BigDecimal exitPrice;

    @Column(name = "position_size", nullable = false, precision = 18, scale = 8)
    private BigDecimal positionSize;

    @Column(name = "take_profit", precision = 18, scale = 8)
    private BigDecimal takeProfit;

    @Column(name = "stop_loss", precision = 18, scale = 8)
    private BigDecimal stopLoss;

    @Column(name = "planned_rr", precision = 5, scale = 2)
    private BigDecimal plannedRr;

    @Column(name = "actual_rr", precision = 5, scale = 2)
    private BigDecimal actualRr;

    @Column(name = "mfe_price", precision = 18, scale = 8)
    private BigDecimal mfePrice;

    @Column(name = "mae_price", precision = 18, scale = 8)
    private BigDecimal maePrice;

    @Column(name = "commissions", precision = 10, scale = 2)
    private BigDecimal commissions;

    @Column(name = "fees_and_swaps", precision = 10, scale = 2)
    private BigDecimal feesAndSwaps;

    @Column(name = "pnl_gross", precision = 10, scale = 2)
    private BigDecimal pnlGross;

    @Column(name = "pnl_net", precision = 10, scale = 2)
    private BigDecimal pnlNet;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}