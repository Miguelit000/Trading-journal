package com.gomezcapital.trading_journal.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    // Límite estricto: 100 peticiones por minuto por IP
    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private final Map<String, RequestInfo> requestCounts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Usamos el método de tu IpBlockFilter para detectar la IP real (incluso detrás de proxies)
        String ip = IpBlockFilter.extractIp(request);
        Instant now = Instant.now();

        RequestInfo requestInfo = requestCounts.compute(ip, (key, info) -> {
            // Si es la primera petición o ya pasó 1 minuto, reiniciamos el contador
            if (info == null || info.windowStart.isBefore(now.minusSeconds(60))) {
                return new RequestInfo(now, 1);
            }
            // Si sigue en la misma ventana de tiempo, sumamos 1
            info.count++;
            return info;
        });

        if (requestInfo.count > MAX_REQUESTS_PER_MINUTE) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Demasiadas peticiones. Has superado el limite de 100 peticiones por minuto. Intenta mas tarde.\"}");
            return; // 🛑 Cortamos la petición aquí, no llega al controlador ni a la BD
        }

        filterChain.doFilter(request, response);
    }

    private static class RequestInfo {
        Instant windowStart;
        int count;

        RequestInfo(Instant windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}