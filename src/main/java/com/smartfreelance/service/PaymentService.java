package com.smartfreelance.service;

import com.smartfreelance.dto.PaymentRequest;
import com.smartfreelance.dto.PaymentResponse;
import com.smartfreelance.entity.Milestone;
import com.smartfreelance.entity.Payment;
import com.smartfreelance.entity.Project;
import com.smartfreelance.entity.User;
import com.smartfreelance.exception.ResourceNotFoundException;
import com.smartfreelance.repository.MilestoneRepository;
import com.smartfreelance.repository.PaymentRepository;
import com.smartfreelance.repository.ProjectRepository;
import com.smartfreelance.security.CurrentUserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MilestoneRepository milestoneRepository;
    private final ProjectRepository projectRepository;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;

    public PaymentService(PaymentRepository paymentRepository,
                          MilestoneRepository milestoneRepository,
                          ProjectRepository projectRepository,
                          CurrentUserService currentUserService,
                          NotificationService notificationService) {
        this.paymentRepository = paymentRepository;
        this.milestoneRepository = milestoneRepository;
        this.projectRepository = projectRepository;
        this.currentUserService = currentUserService;
        this.notificationService = notificationService;
    }

    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest request) {
        Milestone milestone = milestoneRepository.findById(request.getMilestoneId())
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with id: " + request.getMilestoneId()));

        Project project = projectRepository.findById(milestone.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + milestone.getProjectId()));

        User currentUser = currentUserService.getCurrentUser();
        if ("CLIENT".equalsIgnoreCase(currentUser.getRole()) && !project.getClientId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied: You do not own this project.");
        }

        if (!"APPROVED".equalsIgnoreCase(milestone.getStatus())) {
            throw new IllegalStateException("Payment can only be initiated for approved milestones.");
        }

        if (project.getFreelancerId() == null) {
            throw new IllegalStateException("Project does not have a selected freelancer.");
        }

        Optional<Payment> existingPaymentOpt = paymentRepository.findByMilestoneId(milestone.getId());
        if (existingPaymentOpt.isPresent()) {
            throw new IllegalStateException("Payment for this milestone has already been initiated.");
        }

        Payment payment = new Payment();
        payment.setProjectId(project.getId());
        payment.setMilestoneId(milestone.getId());
        payment.setFreelancerId(project.getFreelancerId());
        payment.setAmount(milestone.getAmount());
        payment.setStatus("PENDING");
        payment.setPaymentDate(LocalDateTime.now().toString());

        Payment savedPayment = paymentRepository.save(payment);
        return mapToPaymentResponse(savedPayment);
    }

    @Transactional
    public PaymentResponse settlePayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        Project project = projectRepository.findById(payment.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + payment.getProjectId()));

        User currentUser = currentUserService.getCurrentUser();
        if ("CLIENT".equalsIgnoreCase(currentUser.getRole()) && !project.getClientId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied: You do not own this project.");
        }

        if (!"PENDING".equalsIgnoreCase(payment.getStatus())) {
            throw new IllegalStateException("Only pending payments can be settled.");
        }

        payment.setStatus("COMPLETED");
        if (payment.getTransactionReference() == null || payment.getTransactionReference().isEmpty()) {
            payment.setTransactionReference("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        payment.setPaymentDate(LocalDateTime.now().toString());

        Payment savedPayment = paymentRepository.save(payment);

        notificationService.createNotification(
                payment.getFreelancerId(),
                "Payment Received",
                "Payment of " + payment.getAmount() + " completed for project.",
                "PAYMENT"
        );

        checkAndUpdateProjectCompletion(project.getId());

        return mapToPaymentResponse(savedPayment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        Project project = projectRepository.findById(payment.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + payment.getProjectId()));

        User currentUser = currentUserService.getCurrentUser();
        if ("CLIENT".equalsIgnoreCase(currentUser.getRole()) && !project.getClientId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied: You do not own this project.");
        }
        if ("FREELANCER".equalsIgnoreCase(currentUser.getRole()) && !payment.getFreelancerId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied: You are not authorized to view this payment.");
        }

        return mapToPaymentResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getClientPaymentHistory() {
        User currentUser = currentUserService.getCurrentUser();
        List<Payment> payments;
        if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            payments = paymentRepository.findAll();
        } else {
            payments = paymentRepository.findByClientId(currentUser.getId());
        }
        return payments.stream().map(this::mapToPaymentResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getFreelancerPaymentHistory() {
        User currentUser = currentUserService.getCurrentUser();
        List<Payment> payments;
        if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            payments = paymentRepository.findAll();
        } else {
            payments = paymentRepository.findByFreelancerId(currentUser.getId());
        }
        return payments.stream().map(this::mapToPaymentResponse).collect(Collectors.toList());
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        PaymentResponse response = PaymentResponse.fromEntity(payment);
        if (response.getProjectTitle() == null && payment.getProjectId() != null) {
            projectRepository.findById(payment.getProjectId())
                    .ifPresent(proj -> response.setProjectTitle(proj.getTitle()));
        }
        if (response.getMilestoneTitle() == null && payment.getMilestoneId() != null) {
            milestoneRepository.findById(payment.getMilestoneId())
                    .ifPresent(m -> response.setMilestoneTitle(m.getTitle()));
        }
        return response;
    }

    private void checkAndUpdateProjectCompletion(Long projectId) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            return;
        }

        List<Milestone> milestones = milestoneRepository.findByProjectId(projectId);
        if (milestones.isEmpty()) {
            return;
        }

        boolean allMilestonesApproved = milestones.stream()
                .allMatch(m -> "APPROVED".equalsIgnoreCase(m.getStatus()));

        if (!allMilestonesApproved) {
            return;
        }

        List<Payment> payments = paymentRepository.findByProjectId(projectId);
        boolean allMilestonesPaid = milestones.stream().allMatch(m ->
                payments.stream().anyMatch(p -> Objects.equals(p.getMilestoneId(), m.getId()) && "COMPLETED".equalsIgnoreCase(p.getStatus()))
        );

        if (allMilestonesPaid) {
            project.setStatus("COMPLETED");
            projectRepository.save(project);
        }
    }
}
