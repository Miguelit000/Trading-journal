package com.gomezcapital.trading_journal.infrastructure.rest.controller;

import com.gomezcapital.trading_journal.application.service.StrategyService;
import com.gomezcapital.trading_journal.domain.model.Strategy;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.CreateStrategyRequest;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.StrategyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/strategies")
@RequiredArgsConstructor
@Slf4j
public class StrategyController {

    private final StrategyService strategyService;

    @PostMapping
    public ResponseEntity<StrategyResponse> createStrategy(@RequestBody CreateStrategyRequest request) {
        Strategy strategy = strategyService.creatStrategy(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponseDto(strategy));
    }

    // <-- Endpoint actualizado a /portfolio/{portfolioId}
    @GetMapping("/portfolio/{portfolioId}")
    public ResponseEntity<List<StrategyResponse>> getStrategiesByAccount(@PathVariable UUID portfolioId) {
        List<Strategy> strategies = strategyService.getStrategiesByPortfolioId(portfolioId);
        List<StrategyResponse> responseList = strategies.stream().map(this::toResponseDto).toList();
        return ResponseEntity.ok(responseList);
    }

    private StrategyResponse toResponseDto(Strategy strategy) {
        return new StrategyResponse(
            strategy.id(), strategy.portfolioId(),
            strategy.name(), strategy.description(), strategy.rules()
        );
    }
}