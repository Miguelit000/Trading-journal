package com.gomezcapital.trading_journal.infrastructure.rest.controller;

import com.gomezcapital.trading_journal.application.service.PortfolioService;
import com.gomezcapital.trading_journal.domain.model.Portfolio;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.CreatePortfolioRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping
    public ResponseEntity<List<Portfolio>> getMyPortfolios(Authentication authentication) {
        return ResponseEntity.ok(portfolioService.getUserPortfolios(authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<Portfolio> createPortfolio(Authentication authentication, @RequestBody CreatePortfolioRequest request) {
        // Actualizamos la llamada para incluir targetBalance que viene en el DTO
        Portfolio portfolio = portfolioService.createPortfolio(
                authentication.getName(), 
                request.name(), 
                request.initialBalance(), 
                request.targetBalance(), // <-- NUEVO PARÁMETRO ENVIADO AL SERVICIO
                request.currency()
        );
        return ResponseEntity.ok(portfolio);
    }
}