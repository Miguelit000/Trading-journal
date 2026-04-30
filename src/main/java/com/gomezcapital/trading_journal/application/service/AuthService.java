package com.gomezcapital.trading_journal.application.service;

import com.gomezcapital.trading_journal.domain.model.User;
import com.gomezcapital.trading_journal.domain.ports.UserRepositoryPort;
import com.gomezcapital.trading_journal.domain.ports.PortfolioRepositoryPort;
import com.gomezcapital.trading_journal.domain.model.Portfolio;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.AuthenticationResponse;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.LoginRequest;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.RegisterRequest;
import com.gomezcapital.trading_journal.infrastructure.security.JwtService;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepositoryPort userRepositoryPort;
    private final PortfolioRepositoryPort portfolioRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${google.client.id}")
    private String googleClientId;

    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepositoryPort.existsByEmail(request.email())) {
            throw new IllegalArgumentException("El email ya está registrado.");
        }

        String aliasTemporal = request.email().split("@")[0];

        // <-- AÑADIMOS "ROLE_FREE" -->
        User newUser = new User(null, request.email(), passwordEncoder.encode(request.password()), aliasTemporal, "ROLE_FREE");
        User savedUser = userRepositoryPort.save(newUser);

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

    public AuthenticationResponse authenticateWithGoogle(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail(); 

                Optional<User> userOptional = userRepositoryPort.findByEmail(email);
                User user;
                UUID portfolioId;
                
                if (userOptional.isPresent()) {
                    user = userOptional.get();
                    var portfolios = portfolioRepositoryPort.findByUserId(user.id());
                    if (portfolios.isEmpty()) {
                        Portfolio newPortfolio = new Portfolio(
                            null, user.id(), "Portafolio Principal", 
                            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "USD", LocalDateTime.now()
                        );
                        portfolioId = portfolioRepositoryPort.save(newPortfolio).id();
                    } else {
                        portfolioId = portfolios.get(0).id();
                    }
                } else {
                    String aliasTemporal = email.split("@")[0];
                    // <-- AÑADIMOS "ROLE_FREE" -->
                    user = new User(
                            null, 
                            email, 
                            passwordEncoder.encode(UUID.randomUUID().toString()), 
                            aliasTemporal,
                            "ROLE_FREE" 
                    );
                    user = userRepositoryPort.save(user);

                    Portfolio defaultPortfolio = new Portfolio(
                        null, user.id(), "Portafolio Principal", 
                        BigDecimal.valueOf(10000.00), BigDecimal.valueOf(10000.00), BigDecimal.valueOf(20000.00), "USD", LocalDateTime.now()
                    );
                    portfolioId = portfolioRepositoryPort.save(defaultPortfolio).id();
                }

                String jwtToken = jwtService.generateToken(user.email());
                return new AuthenticationResponse(jwtToken, portfolioId);
            } else {
                throw new IllegalArgumentException("El token de Google es inválido o ha caducado.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error verificando token de Google: " + e.getMessage());
        }
    }
}