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
    private final IpBlockFilter ipBlockFilter; // <-- AÑADIDO: Inyectamos el escudo perimetral

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Activacion del CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Desactivamos CSFR 
            .csrf(AbstractHttpConfigurer::disable)
            // Reglas de las URLs (Respetando tus rutas originales)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**", "/api/v1/webhooks/**").permitAll() // Rutas de Login/Rgistro son publicas
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/api/v1/trades/images/**").permitAll() // Tus imágenes siguen siendo públicas
                .requestMatchers("/api/v1/admin/**").hasAuthority("ROLE_ADMIN") // Panel de ciberseguridad
                .anyRequest().authenticated() // Cualquier otra ruta exige token valido
            )
            // Establecer la Stateless
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // Insertar nuestros filtros: PRIMERO el de IP (Escudo), LUEGO el JWT
            .addFilterBefore(ipBlockFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
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

        // Metodos HTTP (Estrictamente los que usa la API REST)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // CABECERAS ESTRICTAMENTE PERMITIDAS
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",                 
            "Content-Type",                  
            "X-Requested-With",              
            "Accept",                        
            "Origin",                        
            "Access-Control-Request-Method", 
            "Access-Control-Request-Headers" 
        ));
        
        // Aplicar las reglas a toda la API
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    @Bean 
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}