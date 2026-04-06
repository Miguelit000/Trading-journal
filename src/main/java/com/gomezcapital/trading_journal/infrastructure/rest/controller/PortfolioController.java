package com.gomezcapital.trading_journal.infrastructure.rest.controller;

import com.gomezcapital.trading_journal.application.service.PortfolioService;
import com.gomezcapital.trading_journal.domain.model.Portfolio;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.CreatePortfolioRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j // <-- NUEVO: Activamos el registro de logs de consola
@RestController
@RequestMapping("/api/v1/portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping("/me")
    public ResponseEntity<List<Portfolio>> getMyPortfolios(Authentication authentication) {
        return ResponseEntity.ok(portfolioService.getUserPortfolios(authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<Portfolio> createPortfolio(@RequestBody CreatePortfolioRequest request, Authentication authentication) {
        log.info("API REST: Petición recibida para crear Bóveda: [{}] de usuario [{}]", request.name(), authentication.getName());
        
        try {
            Portfolio portfolio = portfolioService.createPortfolio(
                    authentication.getName(), request.name(), request.initialBalance(), request.currency()
            );
            log.info("✅ Bóveda creada con éxito: {}", portfolio.id());
            return ResponseEntity.ok(portfolio);
            
        } catch (Exception e) {
            log.error("❌ ERROR FATAL al crear el portafolio en la base de datos: ", e);
            throw e; // Lanza el error para que React lo vea y lance la alerta
        }
    }
}