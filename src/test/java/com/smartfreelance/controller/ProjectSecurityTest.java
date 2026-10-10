package com.smartfreelance.controller;

import com.smartfreelance.dto.ProjectRequest;
import com.smartfreelance.entity.User;
import com.smartfreelance.repository.UserRepository;
import com.smartfreelance.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProjectSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private String clientToken;
    private String freelancerToken;
    private Long clientId;

    @BeforeEach
    void setUp() {
        User client = userRepository.findByEmail("client_proj_sec@example.com").orElseGet(() -> {
            User u = new User();
            u.setName("Client Project Sec");
            u.setEmail("client_proj_sec@example.com");
            u.setPassword("hashedpassword");
            u.setRole("CLIENT");
            u.setActive(true);
            return userRepository.save(u);
        });
        clientId = client.getId();
        clientToken = "Bearer " + jwtService.generateToken(client.getId(), client.getEmail(), client.getRole());

        User freelancer = userRepository.findByEmail("freelancer_proj_sec@example.com").orElseGet(() -> {
            User u = new User();
            u.setName("Freelancer Project Sec");
            u.setEmail("freelancer_proj_sec@example.com");
            u.setPassword("hashedpassword");
            u.setRole("FREELANCER");
            u.setActive(true);
            return userRepository.save(u);
        });
        freelancerToken = "Bearer " + jwtService.generateToken(freelancer.getId(), freelancer.getEmail(), freelancer.getRole());
    }

    @Test
    @DisplayName("Public project browsing: GET /api/projects is accessible without authentication")
    void testPublicBrowseProjects() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Public project filtering: GET /api/projects/page is accessible without authentication")
    void testPublicBrowseProjectsPaginated() throws Exception {
        mockMvc.perform(get("/api/projects/page")
                        .param("page", "0")
                        .param("size", "10")
                        .param("status", "OPEN"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Public project details: GET /api/projects/{id} is accessible without authentication (not 401)")
    void testPublicGetProjectByIdNotUnauthorized() throws Exception {
        // Even for a non-existent ID, it should return 404 (ResourceNotFound), never 401 (Unauthorized)
        mockMvc.perform(get("/api/projects/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("CORS allows Docker frontend origin http://localhost")
    void testCorsAllowsDockerFrontendOrigin() throws Exception {
        mockMvc.perform(get("/api/projects")
                        .header("Host", "localhost:8081")
                        .header("Origin", "http://localhost"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost"));
    }

    @Test
    @DisplayName("CORS allows Vite dev origin http://localhost:5173")
    void testCorsAllowsViteFrontendOrigin() throws Exception {
        mockMvc.perform(get("/api/projects")
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    @DisplayName("Protected mutation: POST /api/projects without token returns 401 Unauthorized")
    void testCreateProjectUnauthenticatedReturns401() throws Exception {
        ProjectRequest request = new ProjectRequest();
        request.setTitle("Unauthenticated Project");
        request.setDescription("Should not be allowed without login.");
        request.setBudget("1000.00");
        request.setDeadline(LocalDate.now().plusDays(14).toString());
        request.setClientId(clientId);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Role restriction: POST /api/projects with FREELANCER role returns 403 Forbidden")
    void testCreateProjectFreelancerReturns403() throws Exception {
        ProjectRequest request = new ProjectRequest();
        request.setTitle("Freelancer Project");
        request.setDescription("Freelancer should not create projects.");
        request.setBudget("1000.00");
        request.setDeadline(LocalDate.now().plusDays(14).toString());
        request.setClientId(clientId);

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Authorized mutation: POST /api/projects with CLIENT role succeeds with 201 Created")
    void testCreateProjectClientReturns201() throws Exception {
        ProjectRequest request = new ProjectRequest();
        request.setTitle("Valid Client Project");
        request.setDescription("Valid project created by authenticated client.");
        request.setBudget("1200.00");
        request.setDeadline(LocalDate.now().plusDays(20).toString());
        request.setClientId(clientId);

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Other protected resources still reject unauthenticated requests with 401")
    void testOtherProtectedResourcesEnforce401() throws Exception {
        mockMvc.perform(get("/api/proposals/page"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/milestones/project/1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/payments/project/1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/ai/chat"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }
}
