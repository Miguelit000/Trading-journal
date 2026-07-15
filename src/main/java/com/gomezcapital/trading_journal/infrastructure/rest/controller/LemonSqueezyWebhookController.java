package com.gomezcapital.trading_journal.infrastructure.rest.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gomezcapital.trading_journal.domain.model.User;
import com.gomezcapital.trading_journal.domain.ports.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
public class LemonSqueezyWebhookController {

    private final UserRepositoryPort userRepositoryPort;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${lemonsqueezy.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/lemonsqueezy")
    public ResponseEntity<String> handleLemonSqueezyWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Signature", defaultValue = "") String signature) {

        // 1. VALIDACIÓN CRIPTOGRÁFICA (Prevención de Spoofing - OWASP/NIST)
        if (!isValidSignature(payload, signature)) {
            log.error("⚠️ [WEBHOOK] Firma HMAC inválida. Intento de alteración de permisos bloqueado.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Firma inválida");
        }

        try {
            JsonNode rootNode = objectMapper.readTree(payload);
            JsonNode metaNode = rootNode.path("meta");
            String eventName = metaNode.path("event_name").asText();
            
            JsonNode dataNode = rootNode.path("data");
            JsonNode attributesNode = dataNode.path("attributes");
            
            // Extraer de forma segura la inyección customizada que enviamos en el checkout
            JsonNode customData = metaNode.path("custom_data");
            String userIdString = customData.path("user_id").asText(null);
            String userEmail = attributesNode.path("user_email").asText(null);

            log.info("🔔 [WEBHOOK] Evento verificado recibido de Lemon Squeezy: {}", eventName);

            // 2. LÓGICA DE NEGOCIO (Control de Acceso de Roles)
            if ("subscription_created".equals(eventName) || "order_created".equals(eventName)) {
                changeUserRole(userIdString, userEmail, "ROLE_PRO");
            } 
            else if ("subscription_cancelled".equals(eventName) || "subscription_expired".equals(eventName)) {
                changeUserRole(userIdString, userEmail, "ROLE_FREE");
            }

            return ResponseEntity.ok("Webhook validado y procesado exitosamente");

        } catch (Exception e) {
            log.error("❌ [WEBHOOK] Error interno al procesar el JSON validado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error estructural en el Webhook");
        }
    }

    /**
     * Motor Criptográfico: Genera un hash HMAC-SHA256 del payload recibido 
     * utilizando el secreto del servidor, y lo compara con la cabecera 'X-Signature'.
     */
    private boolean isValidSignature(String payload, String signatureHeader) {
        if (signatureHeader == null || signatureHeader.isEmpty()) return false;
        
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            byte[] hash = sha256_HMAC.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString().equals(signatureHeader);
        } catch (Exception e) {
            log.error("Error criptográfico calculando la firma HMAC: {}", e.getMessage());
            return false;
        }
    }

    private void changeUserRole(String userIdString, String backupEmail, String newRole) {
        boolean userUpdated = false;

        if (userIdString != null && !userIdString.isEmpty() && !userIdString.equals("null")) {
            try {
                UUID userId = UUID.fromString(userIdString);
                Optional<User> userOptional = userRepositoryPort.findById(userId);
                
                if (userOptional.isPresent()) {
                    updateRoleInDatabase(userOptional.get(), newRole);
                    userUpdated = true;
                    log.info("✅ Rol actualizado a {} para el usuario {} (ID verificado).", newRole, userOptional.get().email());
                }
            } catch (Exception e) {
                log.error("❌ Error procesando UUID validado en Webhook: {}", e.getMessage());
            }
        }

        if (!userUpdated && backupEmail != null && !backupEmail.isEmpty()) {
             Optional<User> userOptional = userRepositoryPort.findByEmail(backupEmail);
             if (userOptional.isPresent()) {
                 updateRoleInDatabase(userOptional.get(), newRole);
                 log.info("✅ Rol actualizado a {} para el usuario {} (Email de respaldo).", newRole, backupEmail);
             }
        } else if (!userUpdated) {
            log.warn("⚠️ [WEBHOOK] No se aplicó el rol {}. Ausencia total de datos de identificación seguros.", newRole);
        }
    }

    private void updateRoleInDatabase(User user, String newRole) {
        User updatedUser = new User(
                user.id(), user.email(), user.passwordHash(), 
                user.alias(), newRole
        );
        userRepositoryPort.save(updatedUser);
    }
}