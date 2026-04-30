package com.gomezcapital.trading_journal.application.service;

import com.gomezcapital.trading_journal.domain.model.User;
import com.gomezcapital.trading_journal.domain.ports.UserRepositoryPort;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.model.CustomerSearchResult;
import com.stripe.param.CustomerSearchParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// ¡ELIMINAMOS LOS IMPORTS DUPLICADOS DEL PORTAL PARA EVITAR CHOQUES!

@Service
@RequiredArgsConstructor
public class StripeService {

    private final UserRepositoryPort userRepositoryPort;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.price.pro.id}")
    private String proPriceId;

    @Value("${stripe.success.url}")
    private String successUrl;

    @Value("${stripe.cancel.url}")
    private String cancelUrl;

    // Esto inicializa la llave de Stripe apenas arranca Spring Boot
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    public String createCheckoutSession(String userEmail) {
        User user = userRepositoryPort.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        try {
            // Construimos la pasarela de pago para la suscripción
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setCustomerEmail(user.email()) 
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setPrice(proPriceId)
                                    .setQuantity(1L)
                                    .build()
                    )
                    // Metadatos para la Sesión
                    .putMetadata("userId", user.id().toString()) 
                    // Metadatos para la Suscripción
                    .setSubscriptionData(
                        SessionCreateParams.SubscriptionData.builder()
                            .putMetadata("userId", user.id().toString())
                            .build()
                    )
                    .build();

            Session session = Session.create(params);
            
            // Devolvemos la URL segura de Stripe a donde React tiene que enviar al usuario
            return session.getUrl();
            
        } catch (Exception e) {
            throw new RuntimeException("Error al crear la sesión de Stripe: " + e.getMessage());
        }
    }

    public String createCustomerPortalSession(String userEmail) throws Exception {
        // 1. Buscamos al cliente en Stripe usando su correo electrónico
        CustomerSearchParams searchParams = CustomerSearchParams.builder()
                .setQuery("email:'" + userEmail + "'")
                .build();
        
        CustomerSearchResult result = com.stripe.model.Customer.search(searchParams);
        
        if (result.getData().isEmpty()) {
            throw new RuntimeException("No se encontró un perfil de facturación para este correo.");
        }
        
        // Extraemos el ID exacto que Stripe le asignó a tu cliente
        String stripeCustomerId = result.getData().get(0).getId();

        // 2. Creamos el ticket de entrada al Portal de Auto-Servicio.
        // Usamos la "ruta completa" (fully qualified name) para que Java no lo confunda con el Session del Checkout
        com.stripe.param.billingportal.SessionCreateParams params = 
            com.stripe.param.billingportal.SessionCreateParams.builder()
                .setCustomer(stripeCustomerId)
                .setReturnUrl("http://localhost:5173/dashboard") // A dónde vuelve al salir del portal
                .build();

        com.stripe.model.billingportal.Session portalSession = 
            com.stripe.model.billingportal.Session.create(params);

        return portalSession.getUrl();
    }
}