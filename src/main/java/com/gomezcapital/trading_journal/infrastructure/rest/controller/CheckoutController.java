package com.gomezcapital.trading_journal.infrastructure.rest.controller;

import com.gomezcapital.trading_journal.application.service.LemonSqueezyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final LemonSqueezyService lemonSqueezyService;

    @PostMapping("/create-session")
    public ResponseEntity<Map<String, String>> createCheckoutSession() {
        try {
            // Obtenemos de forma segura el email del usuario autenticado por JWT
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            String sessionUrl = lemonSqueezyService.createCheckoutUrl(userEmail);
            
            Map<String, String> response = new HashMap<>();
            response.put("url", sessionUrl);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al crear sesión de pago: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/portal")
    public ResponseEntity<Map<String, String>> createPortalSession() {
        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            String portalUrl = lemonSqueezyService.getCustomerPortalUrl(userEmail);
            
            Map<String, String> response = new HashMap<>();
            response.put("url", portalUrl);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al abrir el portal de cliente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}