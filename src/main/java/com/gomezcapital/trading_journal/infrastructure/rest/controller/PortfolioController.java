package com.gomezcapital.trading_journal.infrastructure.rest.controller;

import com.gomezcapital.trading_journal.application.service.PortfolioService;
import com.gomezcapital.trading_journal.domain.model.Portfolio;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.CreatePortfolioRequest;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.UpdateTargetRequest;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.UpdatePortfolioBalancesRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
        Portfolio portfolio = portfolioService.createPortfolio(
                authentication.getName(), 
                request.name(), 
                request.initialBalance(), 
                request.targetBalance(), 
                request.currency()
        );
        return ResponseEntity.ok(portfolio);
    }

    @PatchMapping("/{id}/target")
    public ResponseEntity<Portfolio> updateTargetBalance(
            @PathVariable UUID id, 
            @RequestBody UpdateTargetRequest request) {
        return ResponseEntity.ok(portfolioService.updateTargetBalance(id, request.targetBalance()));
    }

    // <-- AQUÍ ESTÁ EL ENDPOINT QUE FALTABA PARA EL AJUSTE DE CAPITAL -->
    @PatchMapping("/{id}/balances")
    public ResponseEntity<Portfolio> updateBalances(
            @PathVariable UUID id, 
            @RequestBody UpdatePortfolioBalancesRequest request) {
        return ResponseEntity.ok(portfolioService.updateBalances(
                id, request.initialBalance(), request.currentBalance(), request.targetBalance()
        ));
    }
}