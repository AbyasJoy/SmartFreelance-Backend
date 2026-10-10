package com.smartfreelance.controller;

import com.smartfreelance.config.MDCLoggingFilter;
import com.smartfreelance.entity.User;
import com.smartfreelance.repository.UserRepository;
import com.smartfreelance.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ActuatorAndSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private String adminToken;

    @BeforeEach
    void setUp() {
        User admin = userRepository.findByEmail("admin_actuator@example.com").orElseGet(() -> {
            User u = new User();
            u.setName("Actuator Admin");
            u.setEmail("admin_actuator@example.com");
            u.setPassword("hashedpassword");
            u.setRole("ADMIN");
            u.setActive(true);
            return userRepository.save(u);
        });

        adminToken = jwtService.generateToken(admin.getId(), admin.getEmail(), admin.getRole());
    }

    @Test
    void actuatorHealth_PublicAccess_Returns200OK() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(header().exists(MDCLoggingFilter.CORRELATION_ID_HEADER));
    }

    @Test
    void actuatorInfo_PublicAccess_Returns200OK() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk())
                .andExpect(header().exists(MDCLoggingFilter.CORRELATION_ID_HEADER));
    }

    @Test
    void actuatorMetrics_UnauthenticatedAccess_Returns401Unauthorized() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void actuatorMetrics_AdminAuthenticatedAccess_Returns200OKWithMetrics() throws Exception {
        mockMvc.perform(get("/actuator/metrics")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.names", notNullValue()));
    }

    @Test
    void requestCorrelationId_PreservesClientProvidedHeader() throws Exception {
        String customCorrelationId = "custom-test-req-12345";
        mockMvc.perform(get("/actuator/health")
                        .header(MDCLoggingFilter.CORRELATION_ID_HEADER, customCorrelationId))
                .andExpect(status().isOk())
                .andExpect(header().string(MDCLoggingFilter.CORRELATION_ID_HEADER, customCorrelationId));
    }

    @Test
    void requestCorrelationId_InvalidHeader_ReplacesWithGeneratedUUID() throws Exception {
        String malformedCorrelationId = "invalid_id_with_special_chars_!@#$%^&*()_and_exceeding_max_allowed_length_1234567890";
        mockMvc.perform(get("/actuator/health")
                        .header(MDCLoggingFilter.CORRELATION_ID_HEADER, malformedCorrelationId))
                .andExpect(status().isOk())
                .andExpect(header().string(MDCLoggingFilter.CORRELATION_ID_HEADER, org.hamcrest.Matchers.not(malformedCorrelationId)));
    }
}
