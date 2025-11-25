package ru.abramov.FinFlow.FinFlow.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

@OpenAPIDefinition(
        info = @Info(
                title = "FinFlow API",
                version = "1.0",
                description = "API для управления финансами",
                contact = @Contact(
                        name = "Arseny",
                        email = "arsenijabramov2305@gmail.com"
                )

        ),
        servers = {
                @Server(
                        description = "Local ENV",
                        url = "http://localhost:8080"
                )
        },
        security = {
                @SecurityRequirement(name ="bearerAuth")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "Jwt auth description",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
@Configuration

public class OpenApiConfig {

}
