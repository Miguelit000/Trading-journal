package com.gomezcapital.trading_journal.application.service;

import com.gomezcapital.trading_journal.domain.model.Portfolio;
import com.gomezcapital.trading_journal.domain.model.User;
import com.gomezcapital.trading_journal.domain.ports.PortfolioRepositoryPort;
import com.gomezcapital.trading_journal.domain.ports.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepositoryPort portfolioRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    public List<Portfolio> getUserPortfolios(String userEmail) {
        User user = userRepositoryPort.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return portfolioRepositoryPort.findByUserId(user.id());
    }

    public Portfolio createPortfolio(String userEmail, String name, BigDecimal initialBalance, BigDecimal targetBalance, String currency) {
        User user = userRepositoryPort.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        Portfolio newPortfolio = new Portfolio(
                null, user.id(), name, initialBalance, initialBalance, targetBalance, currency, LocalDateTime.now()
        );
        return portfolioRepositoryPort.save(newPortfolio);
    }

    public Portfolio updateTargetBalance(UUID portfolioId, BigDecimal newTarget) {
        Portfolio portfolio = portfolioRepositoryPort.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portafolio no encontrado"));
        
        Portfolio updatedPortfolio = new Portfolio(
                portfolio.id(), portfolio.userId(), portfolio.name(),
                portfolio.initialBalance(), portfolio.currentBalance(), newTarget,
                portfolio.currency(), portfolio.createdAt()
        );
        return portfolioRepositoryPort.save(updatedPortfolio);
    }

    // <-- NUEVO: ACTUALIZAR TODOS LOS BALANCES (DEPÓSITOS/RETIROS/RESET) -->
    public Portfolio updateBalances(UUID portfolioId, BigDecimal initialBalance, BigDecimal currentBalance, BigDecimal targetBalance) {
        Portfolio portfolio = portfolioRepositoryPort.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portafolio no encontrado"));
        
        Portfolio updatedPortfolio = new Portfolio(
                portfolio.id(), portfolio.userId(), portfolio.name(),
                initialBalance != null ? initialBalance : portfolio.initialBalance(), 
                currentBalance != null ? currentBalance : portfolio.currentBalance(), 
                targetBalance != null ? targetBalance : portfolio.targetBalance(),
                portfolio.currency(), portfolio.createdAt()
        );
        return portfolioRepositoryPort.save(updatedPortfolio);
    }
}