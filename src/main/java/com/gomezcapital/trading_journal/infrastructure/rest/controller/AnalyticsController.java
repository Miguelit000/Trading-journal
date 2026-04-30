package com.gomezcapital.trading_journal.infrastructure.rest.controller;

import com.gomezcapital.trading_journal.application.service.AnalyticsService;
import com.gomezcapital.trading_journal.application.service.PortfolioService;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.AdvancedAnalyticsResponse;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.TradeMetricsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final PortfolioService portfolioService;

    @GetMapping("/portfolio/{portfolioId}")
    public ResponseEntity<TradeMetricsResponse> getMetrics(@PathVariable UUID portfolioId, Authentication authentication) {
        // <-- EL ESCUDO: Si no es su portafolio, lanzará error 403 antes de calcular nada -->
        portfolioService.validatePortfolioOwnership(portfolioId, authentication.getName());
        return ResponseEntity.ok(analyticsService.calculateMetrics(portfolioId));
    }

    @GetMapping("/portfolio/{portfolioId}/advanced")
    public ResponseEntity<AdvancedAnalyticsResponse> getAdvancedAnalytics(@PathVariable UUID portfolioId, Authentication authentication) {
        portfolioService.validatePortfolioOwnership(portfolioId, authentication.getName());
        return ResponseEntity.ok(analyticsService.calculateAdvancedAnalytics(portfolioId));
    }
}