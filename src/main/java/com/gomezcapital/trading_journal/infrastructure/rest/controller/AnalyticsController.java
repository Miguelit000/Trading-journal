package com.gomezcapital.trading_journal.infrastructure.rest.controller;

import com.gomezcapital.trading_journal.application.service.AnalyticsService;
import com.gomezcapital.trading_journal.application.service.PortfolioService;
import com.gomezcapital.trading_journal.domain.model.User;
import com.gomezcapital.trading_journal.domain.ports.UserRepositoryPort;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.AdvancedAnalyticsResponse;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.TradeMetricsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final PortfolioService portfolioService;
    private final UserRepositoryPort userRepositoryPort; // Inyectamos el acceso a usuarios

    @GetMapping("/portfolio/{portfolioId}")
    public ResponseEntity<TradeMetricsResponse> getMetrics(@PathVariable UUID portfolioId, Authentication authentication) {
        portfolioService.validatePortfolioOwnership(portfolioId, authentication.getName());
        return ResponseEntity.ok(analyticsService.calculateMetrics(portfolioId));
    }

    @GetMapping("/portfolio/{portfolioId}/advanced")
    public ResponseEntity<?> getAdvancedAnalytics(@PathVariable UUID portfolioId, Authentication authentication) {
        portfolioService.validatePortfolioOwnership(portfolioId, authentication.getName());
        
        // VALIDACIÓN DE SEGURIDAD: Permitir PRO y ADMIN
        User user = userRepositoryPort.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
                
        if (!"ROLE_PRO".equals(user.role()) && !"ROLE_ADMIN".equals(user.role())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Acceso denegado. Esta métrica requiere una suscripción PRO."));
        }

        return ResponseEntity.ok(analyticsService.calculateAdvancedAnalytics(portfolioId));
    }
}