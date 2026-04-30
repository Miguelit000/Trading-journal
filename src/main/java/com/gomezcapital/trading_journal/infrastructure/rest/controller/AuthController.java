package com.gomezcapital.trading_journal.infrastructure.rest.controller;

import com.gomezcapital.trading_journal.application.service.AuthService;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.AuthenticationResponse;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.GoogleLoginRequest;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.LoginRequest;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // <-- NUEVO ENDPOINT DE GOOGLE -->
    @PostMapping("/google")
    public ResponseEntity<AuthenticationResponse> loginWithGoogle(@RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(authService.authenticateWithGoogle(request.token()));
    }
}