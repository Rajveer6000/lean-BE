package com.lean.lean.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Lean API",
                version = "${swagger.version:v1}",
                description = "REST API documentation for Lean backend services.",
                contact = @Contact(
                        name = "Lean Support",
                        email = "support@lean.com"
                )
        ),
        servers = {
                @Server(
                        url = "${swagger.server-url:http://localhost:8080}",
                        description = "Current environment"
                )
        }
)
public class OpenApiConfig {

    @Bean
    public OpenAPI leanOpenAPI(
            @Value("${swagger.server-url:http://localhost:8080}") String serverUrl,
            @Value("${swagger.version:v1}") String version) {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("Lean API")
                        .version(version)
                        .description("REST API documentation for Lean backend services.")
                        .license(new License().name("Internal Use").url("https://lean.com")))
                .servers(List.of(new io.swagger.v3.oas.models.servers.Server()
                        .url(serverUrl)
                        .description("Current environment")));
    }
}
