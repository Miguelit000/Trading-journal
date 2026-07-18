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

    // <-- NUEVA FUNCIÓN DE SEGURIDAD (MULTITENANT) -->
    public void validatePortfolioOwnership(UUID portfolioId, String userEmail) {
        User user = userRepositoryPort.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        
        Portfolio portfolio = portfolioRepositoryPort.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portafolio no encontrado."));

        if (!portfolio.userId().equals(user.id())) {
            throw new SecurityException("Acceso denegado: Este portafolio no te pertenece.");
        }
    }

    public List<Portfolio> getUserPortfolios(String userEmail) {
        User user = userRepositoryPort.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        return portfolioRepositoryPort.findByUserId(user.id());
    }

    public Portfolio createPortfolio(String userEmail, String name, BigDecimal initialBalance, BigDecimal targetBalance, String currency) {
        User user = userRepositoryPort.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        // <-- RESTRICCIÓN SAAS: Límite para cuentas gratuitas -->
        if ("ROLE_FREE".equals(user.role())) {
            List<Portfolio> currentPortfolios = portfolioRepositoryPort.findByUserId(user.id());
            if (currentPortfolios.size() >= 1) {
                throw new IllegalStateException("Límite alcanzado: Las cuentas gratuitas solo pueden tener 1 portafolio. ¡Mejora a PRO!");
            }
        }

        Portfolio portfolio = new Portfolio(
                null,
                user.id(),
                name,
                initialBalance,
                initialBalance,
                targetBalance,
                currency,
                LocalDateTime.now()
        );
        return portfolioRepositoryPort.save(portfolio);
    }

    public Portfolio updateTargetBalance(UUID portfolioId, BigDecimal newTargetBalance, String userEmail) {
        validatePortfolioOwnership(portfolioId, userEmail); // Blindaje

        Portfolio portfolio = portfolioRepositoryPort.findById(portfolioId).get();
        Portfolio updatedPortfolio = new Portfolio(
                portfolio.id(), portfolio.userId(), portfolio.name(),
                portfolio.initialBalance(), portfolio.currentBalance(), 
                newTargetBalance, portfolio.currency(), portfolio.createdAt()
        );
        return portfolioRepositoryPort.save(updatedPortfolio);
    }

    public Portfolio updateBalances(UUID portfolioId, BigDecimal newInitialBalance, BigDecimal newCurrentBalance, BigDecimal newTargetBalance, String userEmail) {
        validatePortfolioOwnership(portfolioId, userEmail); // Blindaje

        Portfolio portfolio = portfolioRepositoryPort.findById(portfolioId).get();
        Portfolio updatedPortfolio = new Portfolio(
                portfolio.id(), portfolio.userId(), portfolio.name(),
                newInitialBalance, newCurrentBalance, newTargetBalance, 
                portfolio.currency(), portfolio.createdAt()
        );
        return portfolioRepositoryPort.save(updatedPortfolio);
    }

    public void deletePortfolio(UUID portfolioId, String userEmail) {
        validatePortfolioOwnership(portfolioId, userEmail); 
        portfolioRepositoryPort.deleteById(portfolioId);
    }
}