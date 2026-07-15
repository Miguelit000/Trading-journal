package com.gomezcapital.trading_journal.application.service;

import com.gomezcapital.trading_journal.domain.model.User;
import com.gomezcapital.trading_journal.domain.ports.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LemonSqueezyService {

    private final UserRepositoryPort userRepositoryPort;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${lemonsqueezy.api.key}")
    private String apiKey;

    @Value("${lemonsqueezy.store.id}")
    private String storeId;

    @Value("${lemonsqueezy.variant.pro.id}")
    private String variantId;

    @Value("${lemonsqueezy.redirect.url}")
    private String redirectUrl;

    public String createCheckoutUrl(String userEmail) {
        User user = userRepositoryPort.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Accept", "application/vnd.api+json");
        headers.setContentType(MediaType.valueOf("application/vnd.api+json"));

        // Payload bajo el estándar JSON:API requerido por Lemon Squeezy
        // Inyectamos el ID del usuario en 'custom_data' para recuperarlo seguro en el webhook
        String payload = """
            {
              "data": {
                "type": "checkouts",
                "attributes": {
                  "checkout_data": {
                    "email": "%s",
                    "custom": {
                      "user_id": "%s"
                    }
                  },
                  "product_options": {
                    "redirect_url": "%s"
                  }
                },
                "relationships": {
                  "store": { "data": { "type": "stores", "id": "%s" } },
                  "variant": { "data": { "type": "variants", "id": "%s" } }
                }
              }
            }
            """.formatted(user.email(), user.id().toString(), redirectUrl, storeId, variantId);

        HttpEntity<String> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.lemonsqueezy.com/v1/checkouts",
                HttpMethod.POST,
                request,
                Map.class
            );

            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            Map<String, Object> attributes = (Map<String, Object>) data.get("attributes");
            return (String) attributes.get("url");

        } catch (Exception e) {
            log.error("Error de conexión segura con Lemon Squeezy: {}", e.getMessage());
            throw new RuntimeException("Error al generar la pasarela de pago");
        }
    }

    public String getCustomerPortalUrl(String userEmail) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Accept", "application/vnd.api+json");
        
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            // Buscamos al cliente en Lemon Squeezy para extraer su URL de facturación privada
            ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.lemonsqueezy.com/v1/customers?filter[email]=" + userEmail,
                HttpMethod.GET,
                request,
                Map.class
            );

            java.util.List<Map<String, Object>> data = (java.util.List<Map<String, Object>>) response.getBody().get("data");
            if (data == null || data.isEmpty()) {
                 throw new RuntimeException("No se encontró un perfil de facturación para este correo.");
            }
            
            Map<String, Object> attributes = (Map<String, Object>) data.get(0).get("attributes");
            Map<String, Object> urls = (Map<String, Object>) attributes.get("urls");
            return (String) urls.get("customer_portal");

        } catch (Exception e) {
            log.error("Error al obtener la URL del portal: {}", e.getMessage());
            throw new RuntimeException("Error al conectar con el portal de cliente");
        }
    }
}