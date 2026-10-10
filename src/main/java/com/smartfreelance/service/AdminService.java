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
import com.smartfreelance.entity.User;
import com.smartfreelance.exception.ResourceNotFoundException;
import com.smartfreelance.repository.PaymentRepository;
import com.smartfreelance.repository.ProjectRepository;
import com.smartfreelance.repository.ProposalRepository;
import com.smartfreelance.repository.UserRepository;
import com.smartfreelance.security.CurrentUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class AdminService {

    private static final Set<String> ALLOWED_USER_SORT_FIELDS = Set.of("id", "name", "email", "role", "active");
    private static final Set<String> ALLOWED_PROJECT_SORT_FIELDS = Set.of("id", "title", "description", "budget", "deadline", "status", "clientId", "freelancerId");
    private static final Set<String> ALLOWED_PROPOSAL_SORT_FIELDS = Set.of("id", "projectId", "freelancerId", "proposedPrice", "estimatedDays", "status", "coverLetter");
    private static final Set<String> ALLOWED_PAYMENT_SORT_FIELDS = Set.of("id", "projectId", "milestoneId", "freelancerId", "amount", "status");

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProposalRepository proposalRepository;
    private final PaymentRepository paymentRepository;
    private final CurrentUserService currentUserService;

    public AdminService(UserRepository userRepository,
                        ProjectRepository projectRepository,
                        ProposalRepository proposalRepository,
                        PaymentRepository paymentRepository,
                        CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.proposalRepository = proposalRepository;
        this.paymentRepository = paymentRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getUsersPaginated(int page, int size, String sortBy, String sortDir) {
        validatePaginationAndSorting(page, size, sortBy, sortDir, ALLOWED_USER_SORT_FIELDS);

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<User> userPage = userRepository.findAll(pageable);

        return PageResponse.fromPage(userPage, UserResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse updateUserStatus(Long id, UserStatusUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        User currentUser = currentUserService.getCurrentUser();
        if (currentUser.getId().equals(id) && Boolean.FALSE.equals(request.getActive())) {
            throw new IllegalStateException("Admin cannot deactivate their own account.");
        }

        user.setActive(request.getActive());
        User updatedUser = userRepository.save(user);
        return UserResponse.fromEntity(updatedUser);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProjectResponse> getProjectsPaginated(int page, int size, String sortBy, String sortDir) {
        validatePaginationAndSorting(page, size, sortBy, sortDir, ALLOWED_PROJECT_SORT_FIELDS);

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<Project> projectPage = projectRepository.findAll(pageable);

        return PageResponse.fromPage(projectPage, ProjectResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProposalResponse> getProposalsPaginated(int page, int size, String sortBy, String sortDir) {
        validatePaginationAndSorting(page, size, sortBy, sortDir, ALLOWED_PROPOSAL_SORT_FIELDS);

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<com.smartfreelance.entity.Proposal> proposalPage = proposalRepository.findAll(pageable);

        return PageResponse.fromPage(proposalPage, ProposalResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> getPaymentsPaginated(int page, int size, String sortBy, String sortDir) {
        validatePaginationAndSorting(page, size, sortBy, sortDir, ALLOWED_PAYMENT_SORT_FIELDS);

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<Payment> paymentPage = paymentRepository.findAll(pageable);

        return PageResponse.fromPage(paymentPage, PaymentResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboardStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByActive(true);
        long totalClients = userRepository.countByRole(com.smartfreelance.entity.Role.CLIENT);
        long totalFreelancers = userRepository.countByRole(com.smartfreelance.entity.Role.FREELANCER);
        long totalProjects = projectRepository.count();

        Map<String, Long> projectsByStatus = new HashMap<>();
        List<String> statuses = List.of("OPEN", "FREELANCER_SELECTED", "IN_PROGRESS", "COMPLETED", "CANCELLED");
        for (String status : statuses) {
            projectsByStatus.put(status, projectRepository.countByStatus(status));
        }

        long totalProposals = proposalRepository.count();
        long totalCompletedProjects = projectRepository.countByStatus("COMPLETED");
        long totalPayments = paymentRepository.count();
        long totalCompletedPayments = paymentRepository.countByStatus("COMPLETED");

        Double settledAmountDouble = paymentRepository.sumCompletedPaymentAmount();
        String totalSettledAmount = String.format(Locale.US, "%.2f", settledAmountDouble != null ? settledAmountDouble : 0.0);

        return new AdminDashboardResponse(
                totalUsers,
                activeUsers,
                totalClients,
                totalFreelancers,
                totalProjects,
                projectsByStatus,
                totalProposals,
                totalCompletedProjects,
                totalPayments,
                totalCompletedPayments,
                totalSettledAmount
        );
    }

    private void validatePaginationAndSorting(int page, int size, String sortBy, String sortDir, Set<String> allowedFields) {
        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be greater than 0");
        }
        if (size > 50) {
            throw new IllegalArgumentException("Page size must not be greater than 50");
        }
        if (!allowedFields.contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sort field: " + sortBy);
        }
        if (!"asc".equalsIgnoreCase(sortDir) && !"desc".equalsIgnoreCase(sortDir)) {
            throw new IllegalArgumentException("Invalid sort direction: " + sortDir);
        }
    }
}
