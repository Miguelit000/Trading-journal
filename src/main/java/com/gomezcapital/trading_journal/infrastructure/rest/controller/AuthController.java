package com.gomezcapital.trading_journal.infrastructure.rest.controller;

import com.gomezcapital.trading_journal.application.service.AuthService;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.AuthenticationResponse;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.GoogleLoginRequest;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.LoginRequest;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
        // 1. Ejecutamos el servicio y guardamos el resultado en la variable "response"
        AuthenticationResponse response = authService.register(request);

        // 2. Creamos la Cookie
        ResponseCookie cookie = buildSecureCookie(response.token());

        // 3. Retornamos 1 sola vez con la cookie y el JSON modificado
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AuthenticationResponse(null, response.portfolioId()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginRequest request) {
        AuthenticationResponse response = authService.login(request);

        ResponseCookie cookie = buildSecureCookie(response.token());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AuthenticationResponse(null, response.portfolioId()));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthenticationResponse> loginWithGoogle(@RequestBody GoogleLoginRequest request) {
        AuthenticationResponse response = authService.authenticateWithGoogle(request.token());

        ResponseCookie cookie = buildSecureCookie(response.token());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AuthenticationResponse(null, response.portfolioId()));
    }

    // Este es para produccion
    private ResponseCookie buildSecureCookie(String token) {
        return ResponseCookie.from("jwt", token)
                .httpOnly(true)    // Protege contra XSS (JavaScript no la ve)
                .secure(true)      // Obligatorio para HTTPS y dominios cruzados
                .path("/")         // Disponible en toda la ruta
                .maxAge(7 * 24 * 60 * 60) // 7 días de duración
                .sameSite("None")  // Permite que Vercel lea la cookie del backend
                .build();
    }

    /*  Esto es para probar en local

    private ResponseCookie buildSecureCookie(String token) {
        return ResponseCookie.from("jwt", token)
                .httpOnly(true)    // Sigue protegido contra XSS (JavaScript no la ve)
                .secure(false)     // 🔴 CAMBIO: false para que tu navegador la acepte en HTTP (Localhost)
                .path("/")         // Disponible en toda la ruta
                .maxAge(7 * 24 * 60 * 60) // 7 días de duración
                .sameSite("Lax")   // 🔴 CAMBIO: "Lax" es el estándar que permite localhost cruzado
                .build();
    }

    */
}