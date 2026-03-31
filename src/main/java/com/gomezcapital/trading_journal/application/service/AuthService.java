package com.gomezcapital.trading_journal.application.service;

import com.gomezcapital.trading_journal.domain.model.User;
import com.gomezcapital.trading_journal.domain.ports.UserRepositoryPort;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.AuthenticationResponse;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.LoginRequest;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.RegisterRequest;
import com.gomezcapital.trading_journal.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor

public class AuthService {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationResponse register(RegisterRequest request) {
        log.info("Procensando registro para el email: {}", request.email());

        // El email no puede estar repetido
        if (userRepositoryPort.existsByEmail(request.email())) {
            throw new IllegalArgumentException("El email ya esta registrado en la base de datos.");
        }

        // Encriptar la conraseña 
        String hashedPassword = passwordEncoder.encode(request.password());

        // Crear el modelo de dominio
        User newUser = new User(
            null,
            request.email(),
            hashedPassword
        );

        // Guardar en la BD a traves del puerto
        User savedUser = userRepositoryPort.save(newUser);

        // Generar token de bienvenida
        String jwtToken = jwtService.generateToken(savedUser.email());

        return new AuthenticationResponse(jwtToken);
    }

    public AuthenticationResponse login(LoginRequest request) {
        log.info("Procensando login para el email: {}", request.email());

        // Buscar usuario
        User user = userRepositoryPort.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Email o contraseña incorrectos."));

        // Verificar que la contraseña coinicda con el hash
        if (!passwordEncoder.matches(request.password(), user.passwordHash())) {
            throw new IllegalArgumentException("Email o contraseña incorrectos.");
        }

        // Si todo es correcto, se genera un nuevo token JWT
        String jwtToken = jwtService.generateToken(user.email());

        return new AuthenticationResponse(jwtToken);
    }
    
}
