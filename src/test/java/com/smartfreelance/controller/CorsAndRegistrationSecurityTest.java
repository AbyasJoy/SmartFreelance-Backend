package com.smartfreelance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartfreelance.dto.LoginRequest;
import com.smartfreelance.dto.UserRegisterRequest;
import com.smartfreelance.entity.User;
import com.smartfreelance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CorsAndRegistrationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String VERCEL_ORIGIN = "https://smart-freelance-frontend.vercel.app";

    @BeforeEach
    void setUp() {
        if (userRepository.findByEmail("existing_test_user@example.com").isEmpty()) {
            User existing = new User();
            existing.setName("Existing User");
            existing.setEmail("existing_test_user@example.com");
            existing.setPassword(passwordEncoder.encode("Password123!"));
            existing.setRole("CLIENT");
            existing.setActive(true);
            userRepository.save(existing);
        }
    }

    @Test
    void corsPreflight_VercelOrigin_ReturnsAllowedHeadersAndStatus200() throws Exception {
        mockMvc.perform(options("/api/users/register")
                        .header("Origin", VERCEL_ORIGIN)
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Authorization, Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", VERCEL_ORIGIN))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("POST")));
    }

    @Test
    void registerUser_ValidRequest_Returns201Created() throws Exception {
        UserRegisterRequest regReq = new UserRegisterRequest();
        regReq.setName("New Vercel User");
        regReq.setEmail("new_vercel_user@example.com");
        regReq.setPassword("SecurePass123!");
        regReq.setRole("FREELANCER");

        mockMvc.perform(post("/api/users/register")
                        .header("Origin", VERCEL_ORIGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Access-Control-Allow-Origin", VERCEL_ORIGIN))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.email").value("new_vercel_user@example.com"))
                .andExpect(jsonPath("$.role").value("FREELANCER"));
    }

    @Test
    void registerUser_DuplicateEmail_Returns409Conflict() throws Exception {
        UserRegisterRequest regReq = new UserRegisterRequest();
        regReq.setName("Duplicate User");
        regReq.setEmail("existing_test_user@example.com");
        regReq.setPassword("SecurePass123!");
        regReq.setRole("CLIENT");

        mockMvc.perform(post("/api/users/register")
                        .header("Origin", VERCEL_ORIGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("Email is already in use")));
    }

    @Test
    void loginUser_ValidCredentials_Returns200OKAndJwtToken() throws Exception {
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("existing_test_user@example.com");
        loginReq.setPassword("Password123!");

        mockMvc.perform(post("/api/users/login")
                        .header("Origin", VERCEL_ORIGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", VERCEL_ORIGIN))
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.email").value("existing_test_user@example.com"));
    }

    @Test
    void loginUser_InvalidPassword_Returns401Unauthorized() throws Exception {
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("existing_test_user@example.com");
        loginReq.setPassword("WrongPassword!");

        mockMvc.perform(post("/api/users/login")
                        .header("Origin", VERCEL_ORIGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }
}
