package com.gomezcapital.trading_journal.infrastructure.rest.controller;

import com.gomezcapital.trading_journal.application.service.AnalyticsService;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.AdvancedAnalyticsResponse;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.TradeMetricsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // =========================================================
    // ENDPOINT ORIGINAL RESTAURADO (El que lee el Dashboard)
    // =========================================================
    @GetMapping("/portfolio/{portfolioId}")
    public ResponseEntity<TradeMetricsResponse> getMetrics(@PathVariable UUID portfolioId) {
        return ResponseEntity.ok(analyticsService.calculateMetrics(portfolioId));
    }

    // =========================================================
    // NUEVO ENDPOINT AVANZADO (El que lee la página de Estadísticas)
    // =========================================================
    @GetMapping("/portfolio/{portfolioId}/advanced")
    public ResponseEntity<AdvancedAnalyticsResponse> getAdvancedAnalytics(@PathVariable UUID portfolioId) {
        return ResponseEntity.ok(analyticsService.calculateAdvancedAnalytics(portfolioId));
    }
}