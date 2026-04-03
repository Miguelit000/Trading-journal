package com.gomezcapital.trading_journal.application.service;

import com.gomezcapital.trading_journal.domain.model.User;
import com.gomezcapital.trading_journal.domain.ports.UserRepositoryPort;
import com.gomezcapital.trading_journal.domain.ports.AccountRepositoryPort;
import com.gomezcapital.trading_journal.domain.model.Account;
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
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor

public class AuthService {

    private final UserRepositoryPort userRepositoryPort;
    private final AccountRepositoryPort accountRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

   public AuthenticationResponse register(RegisterRequest request) {
        log.info("Procesando registro para el email: {}", request.email());

        if (userRepositoryPort.existsByEmail(request.email())) {
            throw new IllegalArgumentException("El email ya está registrado en la base de datos.");
        }

        String hashedPassword = passwordEncoder.encode(request.password());
        User newUser = new User(null, request.email(), hashedPassword);
        User savedUser = userRepositoryPort.save(newUser);

        // ¡MAGIA ENTERPRISE!: Al registrarse, le creamos automáticamente su primera cuenta
        Account defaultAccount = new Account(
            null, 
            savedUser.id(), 
            "Bóveda Principal", // Nombre por defecto
            BigDecimal.valueOf(10000.00), // Le damos un balance inicial de prueba (10k)
            BigDecimal.valueOf(10000.00), 
            "USD", 
            LocalDateTime.now()
        );
        Account savedAccount = accountRepositoryPort.save(defaultAccount);

        String jwtToken = jwtService.generateToken(savedUser.email());

        // Devolvemos el token Y el nuevo accountId
        return new AuthenticationResponse(jwtToken, savedAccount.id());
    }

    public AuthenticationResponse login(LoginRequest request) {
        log.info("Procesando login para el email: {}", request.email());

        User user = userRepositoryPort.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Email o contraseña incorrectos."));

        if (!passwordEncoder.matches(request.password(), user.passwordHash())) {
            throw new IllegalArgumentException("Email o contraseña incorrectos.");
        }

        String jwtToken = jwtService.generateToken(user.email());

        var accounts = accountRepositoryPort.findByUserId(user.id());
        UUID accountId;

        // ¡AUTO-SANACIÓN!: Si es un usuario viejo (como el admin) y no tiene cuenta, se la creamos al vuelo
        if (accounts.isEmpty()) {
            log.info("El usuario no tenía cuenta. Creando una cuenta por defecto de auto-sanación...");
            Account newAccount = new Account(null, user.id(), "Bóveda Principal", BigDecimal.ZERO, BigDecimal.ZERO, "USD", LocalDateTime.now());
            accountId = accountRepositoryPort.save(newAccount).id();
        } else {
            // Si ya tenía cuenta, simplemente la usamos
            accountId = accounts.get(0).id();
        }

        return new AuthenticationResponse(jwtToken, accountId);
    }
    
}
