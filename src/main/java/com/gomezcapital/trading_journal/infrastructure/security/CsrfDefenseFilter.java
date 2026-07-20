package com.gomezcapital.trading_journal.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class CsrfDefenseFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String method = request.getMethod();

        // 1. Los métodos de lectura no cambian el estado de la BD, son seguros frente a CSRF.
        if ("GET".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Si es el Webhook de LemonSqueezy, lo dejamos pasar porque LemonSqueezy envía "application/vnd.api+json"
        // y además ya tiene su propia validación HMAC criptográfica.
        if (request.getRequestURI().startsWith("/api/v1/webhooks")) {
            filterChain.doFilter(request, response);
            return;
        }

        String contentType = request.getContentType();

        // 3. Excepción permitida: Subida de imágenes de los Trades
        if (contentType != null && contentType.toLowerCase().startsWith("multipart/form-data")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 4. EL ESCUDO: Si no es application/json, es un posible intento de CSRF mediante un formulario web malicioso.
        if (contentType == null || !contentType.toLowerCase().contains("application/json")) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Ataque CSRF mitigado. Solo se permiten payloads en formato application/json.\"}");
            return; // 🛑 Cortamos la conexión inmediatamente
        }

        // Si pasa todas las pruebas, la petición es legítima.
        filterChain.doFilter(request, response);
    }
}