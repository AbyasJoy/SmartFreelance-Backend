package com.smartfreelance.service;

import com.smartfreelance.dto.AdminDashboardResponse;
import com.smartfreelance.dto.PageResponse;
import com.smartfreelance.dto.PaymentResponse;
import com.smartfreelance.dto.ProjectResponse;
import com.smartfreelance.dto.ProposalResponse;
import com.smartfreelance.dto.UserResponse;
import com.smartfreelance.dto.UserStatusUpdateRequest;
import com.smartfreelance.entity.Payment;
import com.smartfreelance.entity.Project;
import com.smartfreelance.entity.Proposal;
import com.smartfreelance.entity.Role;
import com.smartfreelance.entity.User;
import com.smartfreelance.exception.ResourceNotFoundException;
import com.smartfreelance.repository.PaymentRepository;
import com.smartfreelance.repository.ProjectRepository;
import com.smartfreelance.repository.ProposalRepository;
import com.smartfreelance.repository.UserRepository;
import com.smartfreelance.security.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProposalRepository proposalRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private AdminService adminService;

    private User adminUser;
    private User clientUser;
    private User freelancerUser;

    @BeforeEach
    void setUp() {
        adminUser = new User(1L, "Admin User", "admin@example.com", "pass", "ADMIN", true);
        clientUser = new User(2L, "Client User", "client@example.com", "pass", "CLIENT", true);
        freelancerUser = new User(3L, "Freelancer User", "free@example.com", "pass", "FREELANCER", true);
    }

    @Test
    void testGetUsersPaginated_Success() {
        Page<User> userPage = new PageImpl<>(List.of(adminUser, clientUser));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

        PageResponse<UserResponse> result = adminService.getUsersPaginated(0, 10, "id", "asc");

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals("Admin User", result.getContent().get(0).getName());
    }

    @Test
    void testGetUsersPaginated_InvalidPageOrSize_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> adminService.getUsersPaginated(-1, 10, "id", "asc"));
        assertThrows(IllegalArgumentException.class, () -> adminService.getUsersPaginated(0, 0, "id", "asc"));
        assertThrows(IllegalArgumentException.class, () -> adminService.getUsersPaginated(0, 100, "id", "asc"));
        assertThrows(IllegalArgumentException.class, () -> adminService.getUsersPaginated(0, 10, "invalidField", "asc"));
    }

    @Test
    void testGetUserById_Success() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(clientUser));

        UserResponse response = adminService.getUserById(2L);

        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals("Client User", response.getName());
    }

    @Test
    void testGetUserById_NonExistent_ThrowsResourceNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminService.getUserById(999L));
    }

    @Test
    void testUpdateUserStatus_DeactivateOtherUser_Success() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(clientUser));
        when(currentUserService.getCurrentUser()).thenReturn(adminUser);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserStatusUpdateRequest request = new UserStatusUpdateRequest(false);
        UserResponse response = adminService.updateUserStatus(2L, request);

        assertNotNull(response);
        assertFalse(response.isActive());
    }

    @Test
    void testUpdateUserStatus_SelfDeactivation_ThrowsIllegalStateException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(currentUserService.getCurrentUser()).thenReturn(adminUser);

        UserStatusUpdateRequest request = new UserStatusUpdateRequest(false);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> adminService.updateUserStatus(1L, request));
        assertTrue(ex.getMessage().contains("deactivate their own account"));
    }

    @Test
    void testGetProjectsPaginated_Success() {
        Project p1 = new Project(10L, "Proj 1", "Desc 1", "1000", "2026-12-31", "OPEN", 2L, null);
        Page<Project> projectPage = new PageImpl<>(List.of(p1));
        when(projectRepository.findAll(any(Pageable.class))).thenReturn(projectPage);

        PageResponse<ProjectResponse> response = adminService.getProjectsPaginated(0, 10, "id", "asc");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Proj 1", response.getContent().get(0).getTitle());
    }

    @Test
    void testGetProposalsPaginated_Success() {
        Proposal prop = new Proposal(100L, 10L, 3L, "Cover letter", "1000", "5", "PENDING");
        Page<Proposal> proposalPage = new PageImpl<>(List.of(prop));
        when(proposalRepository.findAll(any(Pageable.class))).thenReturn(proposalPage);

        PageResponse<ProposalResponse> response = adminService.getProposalsPaginated(0, 10, "id", "asc");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("1000", response.getContent().get(0).getProposedPrice());
    }

    @Test
    void testGetPaymentsPaginated_Success() {
        Payment p = new Payment(1L, 10L, 100L, 3L, "2500.00", "COMPLETED", "2026-10-09", "TXN-123");
        Page<Payment> paymentPage = new PageImpl<>(List.of(p));
        when(paymentRepository.findAll(any(Pageable.class))).thenReturn(paymentPage);

        PageResponse<PaymentResponse> response = adminService.getPaymentsPaginated(0, 10, "id", "asc");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("COMPLETED", response.getContent().get(0).getStatus());
    }

    @Test
    void testGetDashboardStats_CalculatesCorrectMetrics() {
        when(userRepository.count()).thenReturn(5L);
        when(userRepository.countByActive(true)).thenReturn(4L);
        when(userRepository.countByRole(Role.CLIENT)).thenReturn(2L);
        when(userRepository.countByRole(Role.FREELANCER)).thenReturn(2L);

        when(projectRepository.count()).thenReturn(3L);
        when(projectRepository.countByStatus("OPEN")).thenReturn(1L);
        when(projectRepository.countByStatus("FREELANCER_SELECTED")).thenReturn(1L);
        when(projectRepository.countByStatus("IN_PROGRESS")).thenReturn(0L);
        when(projectRepository.countByStatus("COMPLETED")).thenReturn(1L);
        when(projectRepository.countByStatus("CANCELLED")).thenReturn(0L);

        when(proposalRepository.count()).thenReturn(4L);
        when(paymentRepository.count()).thenReturn(2L);
        when(paymentRepository.countByStatus("COMPLETED")).thenReturn(1L);
        when(paymentRepository.sumCompletedPaymentAmount()).thenReturn(2500.50);

        AdminDashboardResponse stats = adminService.getDashboardStats();

        assertNotNull(stats);
        assertEquals(5L, stats.getTotalUsers());
        assertEquals(4L, stats.getActiveUsers());
        assertEquals(2L, stats.getTotalClients());
        assertEquals(2L, stats.getTotalFreelancers());
        assertEquals(3L, stats.getTotalProjects());
        assertEquals(1L, stats.getTotalCompletedProjects());
        assertEquals(4L, stats.getTotalProposals());
        assertEquals(2L, stats.getTotalPayments());
        assertEquals(1L, stats.getTotalCompletedPayments());
        assertEquals("2500.50", stats.getTotalSettledAmount());
    }

    @Test
    void testGetDashboardStats_EmptyDatabase() {
        when(userRepository.count()).thenReturn(0L);
        when(userRepository.countByActive(true)).thenReturn(0L);
        when(userRepository.countByRole(Role.CLIENT)).thenReturn(0L);
        when(userRepository.countByRole(Role.FREELANCER)).thenReturn(0L);
        when(projectRepository.count()).thenReturn(0L);
        when(proposalRepository.count()).thenReturn(0L);
        when(paymentRepository.count()).thenReturn(0L);
        when(paymentRepository.countByStatus("COMPLETED")).thenReturn(0L);
        when(paymentRepository.sumCompletedPaymentAmount()).thenReturn(0.0);

        AdminDashboardResponse stats = adminService.getDashboardStats();

        assertNotNull(stats);
        assertEquals(0L, stats.getTotalUsers());
        assertEquals("0.00", stats.getTotalSettledAmount());
    }
}
