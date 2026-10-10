package com.smartfreelance.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartfreelance.dto.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FullApplicationIntegrationAuditTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("End-to-End Application Workflow: Registration -> Login -> Project -> Proposal -> Milestone -> Payment -> Review -> AI Chat -> Admin")
    void testFullEndToEndApplicationWorkflow() throws Exception {
        String uniqueSuffix = String.valueOf(System.currentTimeMillis());
        String clientEmail = "client_" + uniqueSuffix + "@example.com";
        String freelancerEmail = "freelancer_" + uniqueSuffix + "@example.com";
        String password = "password123";

        // 1. Register Client Account
        UserRegisterRequest clientReg = new UserRegisterRequest("Audit Client", clientEmail, password, "CLIENT");
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientReg)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(clientEmail))
                .andExpect(jsonPath("$.role").value("CLIENT"));

        // 2. Register Freelancer Account
        UserRegisterRequest freelancerReg = new UserRegisterRequest("Audit Freelancer", freelancerEmail, password, "FREELANCER");
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(freelancerReg)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(freelancerEmail))
                .andExpect(jsonPath("$.role").value("FREELANCER"));

        // 3. Login Client to get JWT Token
        LoginRequest clientLogin = new LoginRequest(clientEmail, password);
        MvcResult clientLoginResult = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        LoginResponse clientResp = objectMapper.readValue(clientLoginResult.getResponse().getContentAsString(), LoginResponse.class);
        String clientToken = "Bearer " + clientResp.getToken();

        // 4. Login Freelancer to get JWT Token
        LoginRequest freelancerLogin = new LoginRequest(freelancerEmail, password);
        MvcResult freelancerLoginResult = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(freelancerLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        LoginResponse freelancerResp = objectMapper.readValue(freelancerLoginResult.getResponse().getContentAsString(), LoginResponse.class);
        String freelancerToken = "Bearer " + freelancerResp.getToken();

        // 5. Client Creates a Project
        ProjectRequest projectReq = new ProjectRequest();
        projectReq.setTitle("Audit Web Development Project " + uniqueSuffix);
        projectReq.setDescription("Building a complete responsive full-stack platform with backend API.");
        projectReq.setBudget("1500.00");
        projectReq.setDeadline(LocalDate.now().plusDays(30).toString());
        projectReq.setClientId(clientResp.getId());

        MvcResult projectResult = mockMvc.perform(post("/api/projects")
                        .header("Authorization", clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andReturn();

        ProjectResponse projectObj = objectMapper.readValue(projectResult.getResponse().getContentAsString(), ProjectResponse.class);
        Long projectId = projectObj.getId();

        // 6. Freelancer Submits a Proposal
        ProposalRequest proposalReq = new ProposalRequest();
        proposalReq.setProjectId(projectId);
        proposalReq.setFreelancerId(freelancerResp.getId());
        proposalReq.setProposedPrice("1400.00");
        proposalReq.setEstimatedDays("14");
        proposalReq.setCoverLetter("I have extensive experience with Spring Boot and React and can deliver on time.");

        MvcResult proposalResult = mockMvc.perform(post("/api/proposals")
                        .header("Authorization", freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(proposalReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        ProposalResponse proposalObj = objectMapper.readValue(proposalResult.getResponse().getContentAsString(), ProposalResponse.class);
        Long proposalId = proposalObj.getId();

        // 7. Client Accepts Proposal
        mockMvc.perform(post("/api/proposals/" + proposalId + "/accept")
                        .header("Authorization", clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        // Verify Project Status is now FREELANCER_SELECTED
        mockMvc.perform(get("/api/projects/" + projectId)
                        .header("Authorization", clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FREELANCER_SELECTED"))
                .andExpect(jsonPath("$.freelancerId").value(freelancerResp.getId()));

        // 8. Client Creates Milestone
        MilestoneRequest milestoneReq = new MilestoneRequest();
        milestoneReq.setProjectId(projectId);
        milestoneReq.setTitle("Phase 1: API & DB Schema");
        milestoneReq.setDescription("Implement core backend models, security, and controllers.");
        milestoneReq.setAmount(new java.math.BigDecimal("700.00"));
        milestoneReq.setDueDate(LocalDate.now().plusDays(10));

        MvcResult milestoneResult = mockMvc.perform(post("/api/milestones")
                        .header("Authorization", clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(milestoneReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        MilestoneResponse milestoneObj = objectMapper.readValue(milestoneResult.getResponse().getContentAsString(), MilestoneResponse.class);
        Long milestoneId = milestoneObj.getId();

        // 9. Freelancer Submits Milestone Work
        MilestoneSubmissionRequest subReq = new MilestoneSubmissionRequest();
        subReq.setSubmissionNotes("Backend REST controllers and JPA repositories implemented with tests.");

        mockMvc.perform(post("/api/milestones/" + milestoneId + "/submit")
                        .header("Authorization", freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"));

        // 10. Client Reviews & Approves Milestone
        MilestoneReviewRequest revReq = new MilestoneReviewRequest();
        revReq.setClientId(clientResp.getId());
        revReq.setAction("APPROVE");
        revReq.setComment("Great work! All requirements met.");

        mockMvc.perform(post("/api/milestones/" + milestoneId + "/review")
                        .header("Authorization", clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(revReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        // 11. Client Verifies Simulated Payment Settled on Milestone Approval
        mockMvc.perform(get("/api/payments/client")
                        .header("Authorization", clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));

        // 12. Check Notifications for Freelancer
        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", freelancerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

        // 13. Query AI Assistant
        AIChatRequest chatReq = new AIChatRequest("How do milestone reviews and simulated payments work?", "");
        mockMvc.perform(post("/api/ai/chat")
                        .header("Authorization", freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").exists());
    }
}
