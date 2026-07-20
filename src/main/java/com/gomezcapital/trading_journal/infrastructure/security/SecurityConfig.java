package com.gomezcapital.trading_journal.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final IpBlockFilter ipBlockFilter;
    private final RateLimitFilter rateLimitFilter; // Opcional, asumo que lo inyectaste en el paso anterior
    private final CsrfDefenseFilter csrfDefenseFilter; // <-- 1. INYECTAMOS EL NUEVO FILTRO

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Activacion del CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // La protección clásica de CSRF se desactiva porque dependemos de cookies cross-domain y APIs sin estado.
            // En su lugar, usamos nuestro propio CsrfDefenseFilter.
            .csrf(AbstractHttpConfigurer::disable)
            
            // Reglas de las URLs
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**", "/api/v1/webhooks/**").permitAll() 
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/api/v1/trades/images/**").permitAll() 
                .requestMatchers("/api/v1/admin/**").hasAuthority("ROLE_ADMIN") 
                .anyRequest().authenticated() 
            )
            // Establecer API sin estado
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // 2. ORDEN DE LOS FILTROS (Importante para la cascada defensiva)
            .addFilterBefore(ipBlockFilter, UsernamePasswordAuthenticationFilter.class) // Nivel 1: Cortafuegos de IP
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class) // Nivel 2: Antispam/DDoS
            .addFilterBefore(csrfDefenseFilter, UsernamePasswordAuthenticationFilter.class) // Nivel 3: Validar que no sea un ataque CSRF
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); // Nivel 4: Validar Identidad (Cookies)
        
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000", 
            "http://localhost:5173", 
            "http://localhost:5174",
            "https://gomez-camipat-web.vercel.app" 
        ));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",                 
            "Content-Type",                  
            "X-Requested-With",              
            "Accept",                        
            "Origin",                        
            "Access-Control-Request-Method", 
            "Access-Control-Request-Headers" 
        ));
        
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    @Bean 
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}