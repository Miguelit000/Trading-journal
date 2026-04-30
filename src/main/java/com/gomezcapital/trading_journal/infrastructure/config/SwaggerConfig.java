package com.gomezcapital.trading_journal.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(

    info = @Info(
        title = "API de Trading Journal",
        version = "1.0",
        description = "Documentacion interactiva del motor de Trading Journal."

    ),

    // Todas las rutas por defecto requieren el candado
    security = {@SecurityRequirement(name = "bearerAuth")}
)

@SecurityScheme(

    name = "bearerAuth",
    description = "Token para rutas privadas",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"

)

public class SwaggerConfig {
    
}
