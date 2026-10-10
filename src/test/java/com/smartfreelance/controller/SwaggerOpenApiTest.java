package com.smartfreelance.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SwaggerOpenApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("OpenAPI JSON endpoint /v3/api-docs is publicly accessible and contains API metadata and security schema")
    void testOpenApiJsonEndpointAccessibleAndValid() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SmartFreelance API")))
                .andExpect(content().string(containsString("v1")))
                .andExpect(content().string(containsString("bearerAuth")))
                .andExpect(content().string(containsString("bearerFormat")))
                .andExpect(content().string(containsString("/api/users/login")))
                .andExpect(content().string(containsString("/api/projects")))
                .andExpect(content().string(containsString("/api/ai/chat")));
    }

    @Test
    @DisplayName("Swagger UI endpoint /swagger-ui.html is publicly accessible without authentication")
    void testSwaggerUiEndpointAccessible() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Swagger UI index page /swagger-ui/index.html is publicly accessible")
    void testSwaggerUiIndexAccessible() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Protected business endpoints still enforce HTTP 401 when unauthenticated")
    void testProtectedEndpointsEnforceAuthentication() throws Exception {
        mockMvc.perform(post("/api/projects"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/ai/chat"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Public endpoints like /api/health, /api/users/register, and public project browsing remain accessible")
    void testPublicEndpointsAccessible() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/projects/page"))
                .andExpect(status().isOk());
    }
}
