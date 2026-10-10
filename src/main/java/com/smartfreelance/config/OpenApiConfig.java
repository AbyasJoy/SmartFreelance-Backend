package com.smartfreelance.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SmartFreelance API")
                        .version("v1")
                        .description("Smart Freelance Project Marketplace REST API with AI Assistant (Gemini Integration & RAG Basics). " +
                                "Supports role-based authorization for CLIENT, FREELANCER, and ADMIN workflows including project management, " +
                                "proposals, milestone deliverables, simulated payment escrow, reviews, notifications, and AI chat.")
                        .contact(new Contact()
                                .name("SmartFreelance Development Team")
                                .email("support@smartfreelance.com")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter your JWT token obtained from POST /api/users/login or POST /api/users/register")));
    }
}
