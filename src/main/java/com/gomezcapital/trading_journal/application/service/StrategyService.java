package com.gomezcapital.trading_journal.application.service;

import com.gomezcapital.trading_journal.domain.model.Strategy;
import com.gomezcapital.trading_journal.domain.ports.StrategyRepositoryPort;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.CreateStrategyRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StrategyService {

    private final StrategyRepositoryPort strategyRepositoryPort;

    public Strategy creatStrategy(CreateStrategyRequest request) {
        log.info("Creando nueva estrategia: {}", request.name());
        Strategy newStrategy = new Strategy(
            null, request.portfolioId(), request.name(), request.description(), request.rules()
        );
        return strategyRepositoryPort.save(newStrategy);
    }

    // <-- AQUÍ ESTABA EL ERROR: El método ya tiene el nombre correcto
    public List<Strategy> getStrategiesByPortfolioId(UUID portfolioId) {
        return strategyRepositoryPort.findByPortfolioId(portfolioId);
    }
}