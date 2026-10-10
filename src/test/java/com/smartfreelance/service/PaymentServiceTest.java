package com.smartfreelance.service;

import com.smartfreelance.dto.PaymentRequest;
import com.smartfreelance.dto.PaymentResponse;
import com.smartfreelance.entity.Milestone;
import com.smartfreelance.entity.Payment;
import com.smartfreelance.entity.Project;
import com.smartfreelance.entity.User;
import com.smartfreelance.repository.MilestoneRepository;
import com.smartfreelance.repository.PaymentRepository;
import com.smartfreelance.repository.ProjectRepository;
import com.smartfreelance.security.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private MilestoneRepository milestoneRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PaymentService paymentService;

    private User clientUser;
    private User freelancerUser;
    private User otherClientUser;
    private Project project;
    private Milestone approvedMilestone;
    private Milestone pendingMilestone;

    @BeforeEach
    void setUp() {
        clientUser = new User(1L, "Client User", "client@example.com", "pass", "CLIENT", true);
        freelancerUser = new User(2L, "Freelancer User", "free@example.com", "pass", "FREELANCER", true);
        otherClientUser = new User(3L, "Other Client", "other@example.com", "pass", "CLIENT", true);

        project = new Project();
        project.setId(10L);
        project.setTitle("Test Project");
        project.setClientId(1L);
        project.setFreelancerId(2L);
        project.setStatus("FREELANCER_SELECTED");

        approvedMilestone = new Milestone();
        approvedMilestone.setId(100L);
        approvedMilestone.setProjectId(10L);
        approvedMilestone.setTitle("Approved Milestone");
        approvedMilestone.setAmount("2500.00");
        approvedMilestone.setStatus("APPROVED");

        pendingMilestone = new Milestone();
        pendingMilestone.setId(101L);
        pendingMilestone.setProjectId(10L);
        pendingMilestone.setTitle("Pending Milestone");
        pendingMilestone.setAmount("1000.00");
        pendingMilestone.setStatus("PENDING");
    }

    @Test
    void testInitiatePayment_Success() {
        when(milestoneRepository.findById(100L)).thenReturn(Optional.of(approvedMilestone));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(currentUserService.getCurrentUser()).thenReturn(clientUser);
        when(paymentRepository.findByMilestoneId(100L)).thenReturn(Optional.empty());

        Payment savedPayment = new Payment(1L, 10L, 100L, 2L, "2500.00", "PENDING", "2026-10-09", null);
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        PaymentRequest request = new PaymentRequest(100L);
        PaymentResponse response = paymentService.initiatePayment(request);

        assertNotNull(response);
        assertEquals("2500.00", response.getAmount());
        assertEquals("PENDING", response.getStatus());
        assertEquals(2L, response.getFreelancerId());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testInitiatePayment_MilestoneNotApproved_ThrowsIllegalStateException() {
        when(milestoneRepository.findById(101L)).thenReturn(Optional.of(pendingMilestone));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(currentUserService.getCurrentUser()).thenReturn(clientUser);

        PaymentRequest request = new PaymentRequest(101L);

        Exception exception = assertThrows(IllegalStateException.class, () -> paymentService.initiatePayment(request));
        assertTrue(exception.getMessage().contains("only be initiated for approved milestones"));
    }

    @Test
    void testInitiatePayment_AnotherClientProject_ThrowsAccessDenied() {
        when(milestoneRepository.findById(100L)).thenReturn(Optional.of(approvedMilestone));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(currentUserService.getCurrentUser()).thenReturn(otherClientUser);

        PaymentRequest request = new PaymentRequest(100L);

        assertThrows(AccessDeniedException.class, () -> paymentService.initiatePayment(request));
    }

    @Test
    void testInitiatePayment_DuplicatePayment_ThrowsIllegalStateException() {
        when(milestoneRepository.findById(100L)).thenReturn(Optional.of(approvedMilestone));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(currentUserService.getCurrentUser()).thenReturn(clientUser);
        when(paymentRepository.findByMilestoneId(100L)).thenReturn(Optional.of(new Payment()));

        PaymentRequest request = new PaymentRequest(100L);

        Exception exception = assertThrows(IllegalStateException.class, () -> paymentService.initiatePayment(request));
        assertTrue(exception.getMessage().contains("already been initiated"));
    }

    @Test
    void testSettlePayment_Success() {
        Payment existingPayment = new Payment(1L, 10L, 100L, 2L, "2500.00", "PENDING", "2026-10-09", null);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(existingPayment));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(currentUserService.getCurrentUser()).thenReturn(clientUser);

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(milestoneRepository.findByProjectId(10L)).thenReturn(List.of(approvedMilestone));
        when(paymentRepository.findByProjectId(10L)).thenReturn(List.of(existingPayment));

        PaymentResponse response = paymentService.settlePayment(1L);

        assertNotNull(response);
        assertEquals("COMPLETED", response.getStatus());
        assertNotNull(response.getTransactionReference());
        assertTrue(response.getTransactionReference().startsWith("TXN-"));

        assertEquals("COMPLETED", project.getStatus());
        verify(projectRepository, times(1)).save(project);
    }

    @Test
    void testSettlePayment_RepeatedSettlement_ThrowsIllegalStateException() {
        Payment completedPayment = new Payment(1L, 10L, 100L, 2L, "2500.00", "COMPLETED", "2026-10-09", "TXN-123456");
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(completedPayment));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(currentUserService.getCurrentUser()).thenReturn(clientUser);

        Exception exception = assertThrows(IllegalStateException.class, () -> paymentService.settlePayment(1L));
        assertTrue(exception.getMessage().contains("Only pending payments can be settled"));
    }

    @Test
    void testGetPaymentById_ClientOwnershipCheck() {
        Payment payment = new Payment(1L, 10L, 100L, 2L, "2500.00", "COMPLETED", "2026-10-09", "TXN-123");
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

        when(currentUserService.getCurrentUser()).thenReturn(clientUser);
        PaymentResponse resClient = paymentService.getPaymentById(1L);
        assertNotNull(resClient);

        when(currentUserService.getCurrentUser()).thenReturn(otherClientUser);
        assertThrows(AccessDeniedException.class, () -> paymentService.getPaymentById(1L));
    }

    @Test
    void testGetPaymentById_FreelancerOwnershipCheck() {
        Payment payment = new Payment(1L, 10L, 100L, 2L, "2500.00", "COMPLETED", "2026-10-09", "TXN-123");
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

        when(currentUserService.getCurrentUser()).thenReturn(freelancerUser);
        PaymentResponse resFree = paymentService.getPaymentById(1L);
        assertNotNull(resFree);

        User otherFreelancer = new User(99L, "Other Free", "otherfree@example.com", "pass", "FREELANCER", true);
        when(currentUserService.getCurrentUser()).thenReturn(otherFreelancer);
        assertThrows(AccessDeniedException.class, () -> paymentService.getPaymentById(1L));
    }
}
