package com.gomezcapital.trading_journal.infrastructure.rest.controller;

import com.gomezcapital.trading_journal.application.service.AnalyticsService;
import com.gomezcapital.trading_journal.domain.model.TradeMetrics;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.TradeMetricsResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor

public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * Endpoint para obtener el resumen del Dashboard.
     * Método HTTP: GET
     * URL: http://localhost:8080/api/v1/analytics/account/{accountId}
     */

    @GetMapping("/account/{accountId}")
    public ResponseEntity<TradeMetricsResponse> getDashboardMetrics(@PathVariable UUID accountId) {
        log.info("API REST: Solicitando metricas del dashboard para la cuenta: {}", accountId);

        TradeMetrics metrics = analyticsService.calculateAccountMetrics(accountId);

        TradeMetricsResponse response = new TradeMetricsResponse(
            metrics.totalTrades(),
            metrics.winningTrades(),
            metrics.losingTrades(),
            metrics.winRate(),
            metrics.profitFactor(),
            metrics.totalPnl()
        );

        return ResponseEntity.ok(response);
    }
}