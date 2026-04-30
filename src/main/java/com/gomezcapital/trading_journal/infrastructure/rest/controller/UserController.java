package com.gomezcapital.trading_journal.infrastructure.rest.controller;

import com.gomezcapital.trading_journal.domain.model.User;
import com.gomezcapital.trading_journal.domain.ports.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepositoryPort userRepositoryPort;

    // Este endpoint mira el token, busca tu correo y devuelve tu Rol real de la Base de Datos
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepositoryPort.findByEmail(email).orElseThrow();

        Map<String, String> response = new HashMap<>();
        response.put("role", user.role());
        response.put("id", user.id().toString());
        
        return ResponseEntity.ok(response);
    }
}