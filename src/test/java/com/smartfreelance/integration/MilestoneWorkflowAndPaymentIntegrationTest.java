package com.smartfreelance.integration;

import com.fasterxml.jackson.core.type.TypeReference;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MilestoneWorkflowAndPaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Complete Milestone Approval, Simulated Payment, Earnings, and Project Completion Workflow")
    void testMilestoneWorkflowAndSimulatedPayments() throws Exception {
        String suffix = String.valueOf(System.currentTimeMillis());
        String clientEmail = "client_" + suffix + "@test.com";
        String freelancerEmail = "freelancer_" + suffix + "@test.com";
        String otherClientEmail = "other_" + suffix + "@test.com";
        String password = "Password123!";

        // 1. Register users
        registerUser("Client User", clientEmail, password, "CLIENT");
        registerUser("Freelancer User", freelancerEmail, password, "FREELANCER");
        registerUser("Other Client", otherClientEmail, password, "CLIENT");

        LoginResponse clientLogin = loginUser(clientEmail, password);
        LoginResponse freelancerLogin = loginUser(freelancerEmail, password);
        LoginResponse otherClientLogin = loginUser(otherClientEmail, password);

        String clientToken = clientLogin.getToken();
        String freelancerToken = freelancerLogin.getToken();
        String otherClientToken = otherClientLogin.getToken();

        // 2. Client creates Project with $950 budget
        ProjectRequest projectRequest = new ProjectRequest(
                "Full Stack Web App " + suffix,
                "Build modern web application",
                "950.00",
                LocalDate.now().plusDays(30).toString(),
                clientLogin.getId()
        );

        MvcResult projectResult = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.budget").value("950.00"))
                .andReturn();

        ProjectResponse project = objectMapper.readValue(projectResult.getResponse().getContentAsString(), ProjectResponse.class);
        Long projectId = project.getId();

        // 3. Freelancer submits Proposal for $1,000
        ProposalRequest proposalRequest = new ProposalRequest(
                projectId,
                freelancerLogin.getId(),
                "12 days delivery with full testing",
                "1000.00",
                "12"
        );

        MvcResult proposalResult = mockMvc.perform(post("/api/proposals")
                        .header("Authorization", "Bearer " + freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(proposalRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.proposedPrice").value("1000.00"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        ProposalResponse proposal = objectMapper.readValue(proposalResult.getResponse().getContentAsString(), ProposalResponse.class);
        Long proposalId = proposal.getId();

        // 4. Client accepts proposal
        mockMvc.perform(post("/api/proposals/" + proposalId + "/accept")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        // Verify project status is FREELANCER_SELECTED
        mockMvc.perform(get("/api/projects/" + projectId)
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FREELANCER_SELECTED"));

        // 5. Client creates Milestone 1 ($10) and Milestone 2 ($990)
        MilestoneRequest m1Req = new MilestoneRequest(
                projectId,
                "Discovery and Research",
                "Initial theme and architecture setup",
                new BigDecimal("10.00"),
                LocalDate.now().plusDays(5)
        );
        MvcResult m1Result = mockMvc.perform(post("/api/milestones")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(m1Req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.amount").value(10.0))
                .andReturn();
        MilestoneResponse m1 = objectMapper.readValue(m1Result.getResponse().getContentAsString(), MilestoneResponse.class);

        MilestoneRequest m2Req = new MilestoneRequest(
                projectId,
                "Final Submission",
                "Complete project delivery and documentation",
                new BigDecimal("990.00"),
                LocalDate.now().plusDays(12)
        );
        MvcResult m2Result = mockMvc.perform(post("/api/milestones")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(m2Req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.amount").value(990.0))
                .andReturn();
        MilestoneResponse m2 = objectMapper.readValue(m2Result.getResponse().getContentAsString(), MilestoneResponse.class);

        // 6. Freelancer submits Milestone 1
        MilestoneSubmissionRequest sub1 = new MilestoneSubmissionRequest("Completed discovery documentation.");
        mockMvc.perform(post("/api/milestones/" + m1.getId() + "/submit")
                        .header("Authorization", "Bearer " + freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sub1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"));

        // 7. Security: Other client cannot review milestone 1
        MilestoneReviewRequest approveReq = new MilestoneReviewRequest("APPROVE", "Looks great!");
        mockMvc.perform(post("/api/milestones/" + m1.getId() + "/review")
                        .header("Authorization", "Bearer " + otherClientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveReq)))
                .andExpect(status().isForbidden());

        // 8. Security: Freelancer cannot review/approve their own milestone
        mockMvc.perform(post("/api/milestones/" + m1.getId() + "/review")
                        .header("Authorization", "Bearer " + freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveReq)))
                .andExpect(status().isForbidden());

        // 9. Client requests revision on Milestone 1
        MilestoneReviewRequest revReq = new MilestoneReviewRequest("REVISION_REQUESTED", "Please add wireframes");
        mockMvc.perform(post("/api/milestones/" + m1.getId() + "/review")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(revReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVISION_REQUESTED"))
                .andExpect(jsonPath("$.reviewComment").value("Please add wireframes"));

        // 10. Freelancer resubmits work for Milestone 1
        MilestoneSubmissionRequest resubReq = new MilestoneSubmissionRequest("Wireframes attached.");
        mockMvc.perform(post("/api/milestones/" + m1.getId() + "/submit")
                        .header("Authorization", "Bearer " + freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resubReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"));

        // 11. Client approves Milestone 1
        mockMvc.perform(post("/api/milestones/" + m1.getId() + "/review")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        // Check payments: exactly 1 payment of $10.00 settled
        MvcResult freelancerPayments1 = mockMvc.perform(get("/api/payments/freelancer")
                        .header("Authorization", "Bearer " + freelancerToken))
                .andExpect(status().isOk())
                .andReturn();
        List<PaymentResponse> payments1 = objectMapper.readValue(
                freelancerPayments1.getResponse().getContentAsString(),
                new TypeReference<List<PaymentResponse>>() {}
        );
        assertThat(payments1).hasSize(1);
        assertThat(payments1.get(0).getMilestoneId()).isEqualTo(m1.getId());
        assertThat(payments1.get(0).getAmount()).isEqualTo("10.00");
        assertThat(payments1.get(0).getStatus()).isEqualTo("COMPLETED");
        assertThat(payments1.get(0).getTransactionReference()).isNotBlank();

        // 12. Idempotency test: approve Milestone 1 again
        mockMvc.perform(post("/api/milestones/" + m1.getId() + "/review")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        // Payments should still have exactly 1 record
        MvcResult freelancerPaymentsIdempotent = mockMvc.perform(get("/api/payments/freelancer")
                        .header("Authorization", "Bearer " + freelancerToken))
                .andExpect(status().isOk())
                .andReturn();
        List<PaymentResponse> paymentsIdempotent = objectMapper.readValue(
                freelancerPaymentsIdempotent.getResponse().getContentAsString(),
                new TypeReference<List<PaymentResponse>>() {}
        );
        assertThat(paymentsIdempotent).hasSize(1);

        // Project should not be completed yet (Milestone 2 still pending)
        mockMvc.perform(get("/api/projects/" + projectId)
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(not("COMPLETED")));

        // 13. Freelancer submits Milestone 2
        MilestoneSubmissionRequest sub2 = new MilestoneSubmissionRequest("Final deliverable completed.");
        mockMvc.perform(post("/api/milestones/" + m2.getId() + "/submit")
                        .header("Authorization", "Bearer " + freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sub2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"));

        // 14. Client approves Milestone 2
        mockMvc.perform(post("/api/milestones/" + m2.getId() + "/review")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        // Check freelancer payments: should have 2 payments, total = $1,000.00
        MvcResult freelancerPayments2 = mockMvc.perform(get("/api/payments/freelancer")
                        .header("Authorization", "Bearer " + freelancerToken))
                .andExpect(status().isOk())
                .andReturn();
        List<PaymentResponse> payments2 = objectMapper.readValue(
                freelancerPayments2.getResponse().getContentAsString(),
                new TypeReference<List<PaymentResponse>>() {}
        );
        assertThat(payments2).hasSize(2);
        double totalSettled = payments2.stream()
                .filter(p -> "COMPLETED".equalsIgnoreCase(p.getStatus()))
                .mapToDouble(p -> Double.parseDouble(p.getAmount()))
                .sum();
        assertThat(totalSettled).isEqualTo(1000.00);

        // Verify project is now automatically COMPLETED
        mockMvc.perform(get("/api/projects/" + projectId)
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // 15. Verify POST /api/projects/{id}/complete is idempotent and succeeds
        mockMvc.perform(post("/api/projects/" + projectId + "/complete")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    private void registerUser(String name, String email, String password, String role) throws Exception {
        UserRegisterRequest reg = new UserRegisterRequest(name, email, password, role);
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());
    }

    private LoginResponse loginUser(String email, String password) throws Exception {
        LoginRequest login = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), LoginResponse.class);
    }
}
