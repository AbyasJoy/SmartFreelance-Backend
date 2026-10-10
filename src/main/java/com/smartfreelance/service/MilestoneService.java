package com.smartfreelance.service;

import com.smartfreelance.dto.MilestoneRequest;
import com.smartfreelance.dto.MilestoneResponse;
import com.smartfreelance.dto.MilestoneReviewRequest;
import com.smartfreelance.dto.MilestoneReviewResponse;
import com.smartfreelance.dto.MilestoneSubmissionRequest;
import com.smartfreelance.dto.MilestoneSubmissionResponse;
import com.smartfreelance.dto.PageResponse;
import com.smartfreelance.entity.Milestone;
import com.smartfreelance.entity.Payment;
import com.smartfreelance.entity.Project;
import com.smartfreelance.entity.User;
import com.smartfreelance.exception.ResourceNotFoundException;
import com.smartfreelance.repository.MilestoneRepository;
import com.smartfreelance.repository.PaymentRepository;
import com.smartfreelance.repository.ProjectRepository;
import com.smartfreelance.security.CurrentUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MilestoneService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "projectId", "title", "description", "amount", "dueDate", "status"
    );

    private final MilestoneRepository milestoneRepository;
    private final ProjectRepository projectRepository;
    private final PaymentRepository paymentRepository;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;

    public MilestoneService(MilestoneRepository milestoneRepository,
                            ProjectRepository projectRepository,
                            PaymentRepository paymentRepository,
                            CurrentUserService currentUserService,
                            NotificationService notificationService) {
        this.milestoneRepository = milestoneRepository;
        this.projectRepository = projectRepository;
        this.paymentRepository = paymentRepository;
        this.currentUserService = currentUserService;
        this.notificationService = notificationService;
    }

    @Transactional
    public MilestoneResponse createMilestone(MilestoneRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + request.getProjectId()));

        User currentUser = currentUserService.getCurrentUser();
        if ("CLIENT".equalsIgnoreCase(currentUser.getRole()) && !project.getClientId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied: You do not own this project");
        }

        Milestone milestone = new Milestone();
        milestone.setProjectId(request.getProjectId());
        milestone.setTitle(request.getTitle());
        milestone.setDescription(request.getDescription());
        milestone.setAmount(request.getAmount() != null ? request.getAmount().toString() : null);
        milestone.setDueDate(request.getDueDate() != null ? request.getDueDate().toString() : null);
        milestone.setStatus("PENDING");

        Milestone savedMilestone = milestoneRepository.save(milestone);
        return MilestoneResponse.fromEntity(savedMilestone);
    }

    @Transactional(readOnly = true)
    public MilestoneResponse getMilestoneById(Long id) {
        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with id: " + id));
        return MilestoneResponse.fromEntity(milestone);
    }

    @Transactional(readOnly = true)
    public List<MilestoneResponse> getMilestonesByProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }

        return milestoneRepository.findByProjectId(projectId)
                .stream()
                .map(MilestoneResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<MilestoneResponse> getMilestonesPaginated(int page, int size, String sortBy, String sortDir) {
        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be greater than 0");
        }
        if (size > 50) {
            throw new IllegalArgumentException("Page size must not be greater than 50");
        }
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sort field: " + sortBy);
        }
        if (!"asc".equalsIgnoreCase(sortDir) && !"desc".equalsIgnoreCase(sortDir)) {
            throw new IllegalArgumentException("Invalid sort direction: " + sortDir);
        }

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<Milestone> milestonePage = milestoneRepository.findAll(pageable);

        return PageResponse.fromPage(milestonePage, MilestoneResponse::fromEntity);
    }

    @Transactional
    public MilestoneResponse updateMilestone(Long id, MilestoneRequest request) {
        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with id: " + id));

        Project project = projectRepository.findById(milestone.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + milestone.getProjectId()));

        User currentUser = currentUserService.getCurrentUser();
        if ("CLIENT".equalsIgnoreCase(currentUser.getRole()) && !project.getClientId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied: You do not own this project");
        }

        if (!"PENDING".equalsIgnoreCase(milestone.getStatus())) {
            throw new RuntimeException("Only pending milestones can be updated.");
        }

        milestone.setTitle(request.getTitle());
        milestone.setDescription(request.getDescription());
        milestone.setAmount(request.getAmount() != null ? request.getAmount().toString() : null);
        milestone.setDueDate(request.getDueDate() != null ? request.getDueDate().toString() : null);

        Milestone updatedMilestone = milestoneRepository.save(milestone);
        return MilestoneResponse.fromEntity(updatedMilestone);
    }

    @Transactional
    public void deleteMilestone(Long id) {
        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with id: " + id));

        Project project = projectRepository.findById(milestone.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + milestone.getProjectId()));

        User currentUser = currentUserService.getCurrentUser();
        if ("CLIENT".equalsIgnoreCase(currentUser.getRole()) && !project.getClientId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied: You do not own this project");
        }

        if (!"PENDING".equalsIgnoreCase(milestone.getStatus())) {
            throw new RuntimeException("Only pending milestones can be deleted.");
        }

        milestoneRepository.delete(milestone);
    }

    @Transactional
    public MilestoneSubmissionResponse submitMilestone(Long milestoneId, MilestoneSubmissionRequest request) {
        return submitMilestone(milestoneId, null, request);
    }

    @Transactional
    public MilestoneSubmissionResponse submitMilestone(Long milestoneId, Long freelancerId, MilestoneSubmissionRequest request) {
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with id: " + milestoneId));

        Project project = projectRepository.findById(milestone.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + milestone.getProjectId()));

        User currentUser = currentUserService.getCurrentUser();
        Long effectiveFreelancerId = currentUser.getId();

        if (project.getFreelancerId() == null) {
            throw new RuntimeException("Project does not have a selected freelancer.");
        }

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole()) && !project.getFreelancerId().equals(effectiveFreelancerId)) {
            throw new AccessDeniedException("Access denied: You are not the selected freelancer for this project.");
        }

        if (!"PENDING".equalsIgnoreCase(milestone.getStatus()) && !"REVISION_REQUESTED".equalsIgnoreCase(milestone.getStatus())) {
            throw new RuntimeException("Only pending or revision-requested milestones can be submitted.");
        }

        milestone.setSubmissionNotes(request.getSubmissionNotes());
        milestone.setStatus("SUBMITTED");

        Milestone savedMilestone = milestoneRepository.save(milestone);

        notificationService.createNotification(
                project.getClientId(),
                "Milestone Submitted",
                "Work submitted for milestone: " + milestone.getTitle(),
                "MILESTONE"
        );

        return MilestoneSubmissionResponse.fromEntity(savedMilestone);
    }

    @Transactional
    public MilestoneReviewResponse reviewMilestone(Long milestoneId, MilestoneReviewRequest request) {
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with id: " + milestoneId));

        Project project = projectRepository.findById(milestone.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + milestone.getProjectId()));

        User currentUser = currentUserService.getCurrentUser();
        if ("CLIENT".equalsIgnoreCase(currentUser.getRole()) && !project.getClientId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied: You do not own this project.");
        }

        if (project.getFreelancerId() == null) {
            throw new RuntimeException("Project does not have a selected freelancer.");
        }

        String action = request.getAction() != null ? request.getAction().trim().toUpperCase() : "";
        if (!"APPROVE".equals(action) && !"REVISION_REQUESTED".equals(action)) {
            throw new IllegalArgumentException("Invalid review action. Supported actions are APPROVE and REVISION_REQUESTED.");
        }

        // Idempotency: repeated approval on an already approved milestone must not fail or create duplicate payments
        if ("APPROVE".equals(action) && "APPROVED".equalsIgnoreCase(milestone.getStatus())) {
            settleMilestonePayment(milestone, project);
            checkAndUpdateProjectCompletion(project.getId());
            return MilestoneReviewResponse.fromEntity(milestone);
        }

        if (!"SUBMITTED".equalsIgnoreCase(milestone.getStatus())) {
            throw new RuntimeException("Only submitted milestones can be reviewed.");
        }

        if ("REVISION_REQUESTED".equals(action)) {
            if (request.getComment() == null || request.getComment().trim().isEmpty()) {
                throw new IllegalArgumentException("Comment is required when requesting revisions.");
            }
            milestone.setStatus("REVISION_REQUESTED");
            milestone.setReviewComment(request.getComment().trim());
            notificationService.createNotification(
                    project.getFreelancerId(),
                    "Milestone Revision Requested",
                    "Revision requested for milestone '" + milestone.getTitle() + "'.",
                    "MILESTONE"
            );
        } else if ("APPROVE".equals(action)) {
            milestone.setStatus("APPROVED");
            if (request.getComment() != null && !request.getComment().trim().isEmpty()) {
                milestone.setReviewComment(request.getComment().trim());
            }
            notificationService.createNotification(
                    project.getFreelancerId(),
                    "Milestone Approved",
                    "Milestone '" + milestone.getTitle() + "' has been approved.",
                    "MILESTONE"
            );

            // Atomically create and settle simulated payment for the approved milestone
            settleMilestonePayment(milestone, project);

            // Check if all milestones are approved and settled to update project completion
            checkAndUpdateProjectCompletion(project.getId());
        }

        Milestone savedMilestone = milestoneRepository.save(milestone);
        return MilestoneReviewResponse.fromEntity(savedMilestone);
    }

    private Payment settleMilestonePayment(Milestone milestone, Project project) {
        Optional<Payment> existingPaymentOpt = paymentRepository.findByMilestoneId(milestone.getId());
        Payment payment;
        if (existingPaymentOpt.isPresent()) {
            payment = existingPaymentOpt.get();
            if (!"COMPLETED".equalsIgnoreCase(payment.getStatus())) {
                payment.setStatus("COMPLETED");
                if (payment.getTransactionReference() == null || payment.getTransactionReference().isEmpty()) {
                    payment.setTransactionReference("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                }
                payment.setPaymentDate(LocalDateTime.now().toString());
                payment = paymentRepository.save(payment);
                notificationService.createNotification(
                        payment.getFreelancerId(),
                        "Payment Received",
                        "Simulated payment of $" + payment.getAmount() + " completed for milestone '" + milestone.getTitle() + "'.",
                        "PAYMENT"
                );
            }
        } else {
            payment = new Payment();
            payment.setProjectId(project.getId());
            payment.setMilestoneId(milestone.getId());
            payment.setFreelancerId(project.getFreelancerId());
            payment.setAmount(milestone.getAmount());
            payment.setStatus("COMPLETED");
            payment.setTransactionReference("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            payment.setPaymentDate(LocalDateTime.now().toString());
            payment = paymentRepository.save(payment);
            notificationService.createNotification(
                    payment.getFreelancerId(),
                    "Payment Received",
                    "Simulated payment of $" + payment.getAmount() + " completed for milestone '" + milestone.getTitle() + "'.",
                    "PAYMENT"
            );
        }
        return payment;
    }

    public void checkAndUpdateProjectCompletion(Long projectId) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null || "COMPLETED".equalsIgnoreCase(project.getStatus())) {
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
            notificationService.createNotification(
                    project.getClientId(),
                    "Project Completed",
                    "Project '" + project.getTitle() + "' has been successfully completed.",
                    "PROJECT"
            );
            if (project.getFreelancerId() != null) {
                notificationService.createNotification(
                        project.getFreelancerId(),
                        "Project Completed",
                        "Project '" + project.getTitle() + "' has been marked as completed.",
                        "PROJECT"
                );
            }
        }
    }
}
