package com.gomezcapital.trading_journal.infrastructure.security;

import com.gomezcapital.trading_journal.application.service.SecurityAuditService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class IpBlockFilter extends OncePerRequestFilter {

    private final SecurityAuditService securityAuditService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = extractIp(request);

        // Si la IP está en la lista negra, bloqueamos al instante
        if (securityAuditService.isIpBlocked(clientIp)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Acceso denegado. IP bloqueada por politicas de ciberseguridad.\"}");
            return; // 🛑 CORTAMOS LA CONEXIÓN AQUÍ.
        }

        // Si la IP es limpia, dejamos pasar la petición
        filterChain.doFilter(request, response);
    }

    public static String extractIp(HttpServletRequest request) {
        // En Render/Vercel, la IP real viene oculta en esta cabecera
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}