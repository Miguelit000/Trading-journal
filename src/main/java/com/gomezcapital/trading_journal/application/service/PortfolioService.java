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

    // Actualizamos la firma del método para recibir "targetBalance"
    public Portfolio createPortfolio(String userEmail, String name, BigDecimal initialBalance, BigDecimal targetBalance, String currency) {
        User user = userRepositoryPort.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        // Pasamos el targetBalance al constructor
        Portfolio newPortfolio = new Portfolio(
                null, user.id(), name, initialBalance, initialBalance, targetBalance, currency, LocalDateTime.now()
        );
        return portfolioRepositoryPort.save(newPortfolio);
    }
}