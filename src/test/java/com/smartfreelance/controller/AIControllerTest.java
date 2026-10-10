package com.smartfreelance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartfreelance.dto.AIChatRequest;
import com.smartfreelance.dto.AIChatResponse;
import com.smartfreelance.entity.User;
import com.smartfreelance.repository.UserRepository;
import com.smartfreelance.security.JwtService;
import com.smartfreelance.service.AIService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private AIService aiService;

    private String validToken;

    @BeforeEach
    void setUp() {
        User testUser = userRepository.findByEmail("testclient_controller@example.com")
                .orElseGet(() -> {
                    User u = new User();
                    u.setName("Test Controller Client");
                    u.setEmail("testclient_controller@example.com");
                    u.setPassword("password123");
                    u.setRole("CLIENT");
                    u.setActive(true);
                    return userRepository.save(u);
                });

        validToken = jwtService.generateToken(testUser.getId(), testUser.getEmail(), testUser.getRole());
    }

    @Test
    void chat_Unauthenticated_ReturnsHttp401() throws Exception {
        AIChatRequest request = new AIChatRequest("How to create a project?");

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void chat_EmptyMessage_ReturnsHttp400() throws Exception {
        AIChatRequest request = new AIChatRequest("");

        mockMvc.perform(post("/api/ai/chat")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.message").exists());
    }

    @Test
    void chat_WhitespaceOnlyMessage_ReturnsHttp400() throws Exception {
        AIChatRequest request = new AIChatRequest("     ");

        mockMvc.perform(post("/api/ai/chat")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.message").exists());
    }

    @Test
    void chat_OversizedMessage_ReturnsHttp400() throws Exception {
        String longMessage = "a".repeat(2001);
        AIChatRequest request = new AIChatRequest(longMessage);

        mockMvc.perform(post("/api/ai/chat")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.message").exists());
    }

    @Test
    void chat_MalformedJsonBody_ReturnsHttp400() throws Exception {
        String malformedJson = "{ \"message\": \"Hello\", ";

        mockMvc.perform(post("/api/ai/chat")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void chat_AuthenticatedValidRequest_ReturnsHttp200AndReply() throws Exception {
        AIChatRequest request = new AIChatRequest("Help me design milestones for my project");

        AIChatResponse mockResponse = new AIChatResponse(
                "Break your project into frontend, backend, and integration milestones.",
                "SUCCESS",
                "gemini-1.5-flash"
        );

        when(aiService.chat(any(AIChatRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/ai/chat")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("Break your project into frontend, backend, and integration milestones."))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.model").value("gemini-1.5-flash"));
    }
}
