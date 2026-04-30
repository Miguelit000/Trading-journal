package com.gomezcapital.trading_journal.infrastructure.rest.controller;

import com.gomezcapital.trading_journal.domain.model.User;
import com.gomezcapital.trading_journal.domain.ports.UserRepositoryPort;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.model.Subscription;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    private final UserRepositoryPort userRepositoryPort;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            log.error("⚠️ [WEBHOOK] Firma inválida.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Firma inválida");
        }

        try {
            // 1. EVENTO DE PAGO O NUEVA SUSCRIPCIÓN (SUBIR A PRO)
            if ("checkout.session.completed".equals(event.getType())) {
                log.info("💰 [WEBHOOK] Procesando pago exitoso...");
                Session session = (Session) event.getDataObjectDeserializer().deserializeUnsafe();

                if (session != null) {
                    String userId = session.getMetadata() != null ? session.getMetadata().get("userId") : null;
                    String email = session.getCustomerEmail();
                    if (email == null && session.getCustomerDetails() != null) {
                        email = session.getCustomerDetails().getEmail();
                    }
                    changeUserRole(userId, email, "ROLE_PRO");
                }
            } 
            else if ("customer.subscription.created".equals(event.getType())) {
                log.info("📈 [WEBHOOK] Procesando nueva suscripción...");
                Subscription subscription = (Subscription) event.getDataObjectDeserializer().deserializeUnsafe();

                if (subscription != null) {
                    String userId = subscription.getMetadata() != null ? subscription.getMetadata().get("userId") : null;
                    changeUserRole(userId, null, "ROLE_PRO");
                }
            }
            // 2. <-- NUEVO EVENTO: CANCELACIÓN DE SUSCRIPCIÓN (BAJAR A FREE) -->
            else if ("customer.subscription.deleted".equals(event.getType())) {
                log.info("📉 [WEBHOOK] ¡Alerta! Una suscripción ha sido cancelada.");
                Subscription subscription = (Subscription) event.getDataObjectDeserializer().deserializeUnsafe();

                if (subscription != null) {
                    String userId = subscription.getMetadata() != null ? subscription.getMetadata().get("userId") : null;
                    
                    // Si perdemos los metadatos al cancelar, usamos un truco para buscar por el ID de cliente de Stripe
                    // Pero asumiendo que viajan bien, usamos nuestro método:
                    changeUserRole(userId, null, "ROLE_FREE");
                }
            }

        } catch (EventDataObjectDeserializationException e) {
            log.error("❌ [WEBHOOK] Error crítico al procesar paquete de Stripe: {}", e.getMessage());
        }

        return ResponseEntity.ok("Webhook procesado");
    }

    // <-- MÉTODO UNIFICADO PARA SUBIR O BAJAR DE ROL -->
    private void changeUserRole(String userIdString, String backupEmail, String newRole) {
        boolean userUpdated = false;

        if (userIdString != null && !userIdString.isEmpty()) {
            try {
                UUID userId = UUID.fromString(userIdString);
                Optional<User> userOptional = userRepositoryPort.findById(userId);
                
                if (userOptional.isPresent()) {
                    updateRoleInDatabase(userOptional.get(), newRole);
                    userUpdated = true;
                    log.info("✅ Rol actualizado a {} para el usuario {} (Por ID).", newRole, userOptional.get().email());
                }
            } catch (Exception e) {
                log.error("❌ Error procesando UUID: {}", e.getMessage());
            }
        }

        if (!userUpdated && backupEmail != null && !backupEmail.isEmpty()) {
             Optional<User> userOptional = userRepositoryPort.findByEmail(backupEmail);
             if(userOptional.isPresent()){
                 updateRoleInDatabase(userOptional.get(), newRole);
                 log.info("✅ Rol actualizado a {} para el usuario {} (Por Email).", newRole, backupEmail);
             }
        } else if (!userUpdated) {
            log.warn("⚠️ [WEBHOOK] No se pudo cambiar el rol a {}. Faltan datos de identificación.", newRole);
        }
    }

    private void updateRoleInDatabase(User user, String newRole) {
        User updatedUser = new User(
                user.id(), user.email(), user.passwordHash(), 
                user.alias(), newRole // Aquí se inyecta ROLE_PRO o ROLE_FREE
        );
        userRepositoryPort.save(updatedUser);
    }
}