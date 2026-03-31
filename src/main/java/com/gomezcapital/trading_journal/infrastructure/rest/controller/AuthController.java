package com.gomezcapital.trading_journal.infrastructure.rest.controller;

import com.gomezcapital.trading_journal.application.service.AuthService;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.AuthenticationResponse;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.LoginRequest;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor

public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("API REST: Peticion de registro recibida para: {}", request.email());
        AuthenticationResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("API REST: Peticion de login recibida para: {}", request.email());
        AuthenticationResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    
}
