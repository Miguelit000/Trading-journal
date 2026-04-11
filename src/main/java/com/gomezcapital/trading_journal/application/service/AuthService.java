package com.gomezcapital.trading_journal.application.service;

import com.gomezcapital.trading_journal.domain.model.User;
import com.gomezcapital.trading_journal.domain.ports.UserRepositoryPort;
import com.gomezcapital.trading_journal.domain.ports.PortfolioRepositoryPort;
import com.gomezcapital.trading_journal.domain.model.Portfolio;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.AuthenticationResponse;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.LoginRequest;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.RegisterRequest;
import com.gomezcapital.trading_journal.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepositoryPort userRepositoryPort;
    private final PortfolioRepositoryPort portfolioRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepositoryPort.existsByEmail(request.email())) {
            throw new IllegalArgumentException("El email ya está registrado.");
        }

        // Generamos un alias temporal basado en su correo
        String aliasTemporal = request.email().split("@")[0];

        User newUser = new User(null, request.email(), passwordEncoder.encode(request.password()), aliasTemporal);
        User savedUser = userRepositoryPort.save(newUser);

        // Creamos el primer Portafolio (Agregamos la meta/target de $20,000 por defecto)
        Portfolio defaultPortfolio = new Portfolio(
            null, savedUser.id(), "Portafolio Principal", 
            BigDecimal.valueOf(10000.00), BigDecimal.valueOf(10000.00), BigDecimal.valueOf(20000.00), "USD", LocalDateTime.now()
        );
        Portfolio savedPortfolio = portfolioRepositoryPort.save(defaultPortfolio);

        return new AuthenticationResponse(jwtService.generateToken(savedUser.email()), savedPortfolio.id());
    }

    public AuthenticationResponse login(LoginRequest request) {
        User user = userRepositoryPort.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales incorrectas."));

        if (!passwordEncoder.matches(request.password(), user.passwordHash())) {
            throw new IllegalArgumentException("Credenciales incorrectas.");
        }

        var portfolios = portfolioRepositoryPort.findByUserId(user.id());
        UUID portfolioId;

        if (portfolios.isEmpty()) {
            // Portafolio vacío si no tiene ninguno (Agregamos la meta/target en ZERO)
            Portfolio newPortfolio = new Portfolio(
                null, user.id(), "Portafolio Principal", 
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "USD", LocalDateTime.now()
            );
            portfolioId = portfolioRepositoryPort.save(newPortfolio).id();
        } else {
            portfolioId = portfolios.get(0).id();
        }

        return new AuthenticationResponse(jwtService.generateToken(user.email()), portfolioId);
    }
}