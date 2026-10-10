package com.smartfreelance.service;

import com.smartfreelance.dto.PageResponse;
import com.smartfreelance.dto.ProjectRequest;
import com.smartfreelance.dto.ProjectResponse;
import com.smartfreelance.entity.Category;
import com.smartfreelance.entity.Project;
import com.smartfreelance.entity.Skill;
import com.smartfreelance.entity.User;
import com.smartfreelance.exception.ResourceNotFoundException;
import com.smartfreelance.entity.Milestone;
import com.smartfreelance.entity.Payment;
import com.smartfreelance.repository.CategoryRepository;
import com.smartfreelance.repository.MilestoneRepository;
import com.smartfreelance.repository.PaymentRepository;
import com.smartfreelance.repository.ProjectRepository;
import com.smartfreelance.repository.SkillRepository;
import com.smartfreelance.repository.UserRepository;
import com.smartfreelance.security.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "title", "description", "budget", "deadline", "status", "categoryId"
    );

    private static final Set<String> VALID_STATUSES = Set.of(
            "OPEN", "FREELANCER_SELECTED", "IN_PROGRESS", "COMPLETED", "CANCELLED", "ALL"
    );

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SkillRepository skillRepository;
    private final CurrentUserService currentUserService;
    private final MilestoneRepository milestoneRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;

    public ProjectService(ProjectRepository projectRepository,
                          UserRepository userRepository,
                          CategoryRepository categoryRepository,
                          SkillRepository skillRepository,
                          CurrentUserService currentUserService) {
        this(projectRepository, userRepository, categoryRepository, skillRepository, currentUserService, null, null, null);
    }

    @Autowired
    public ProjectService(ProjectRepository projectRepository,
                          UserRepository userRepository,
                          CategoryRepository categoryRepository,
                          SkillRepository skillRepository,
                          CurrentUserService currentUserService,
                          MilestoneRepository milestoneRepository,
                          PaymentRepository paymentRepository,
                          NotificationService notificationService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.skillRepository = skillRepository;
        this.currentUserService = currentUserService;
        this.milestoneRepository = milestoneRepository;
        this.paymentRepository = paymentRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        Long effectiveClientId = currentUser.getId();

        if ("ADMIN".equalsIgnoreCase(currentUser.getRole()) && request.getClientId() != null) {
            effectiveClientId = request.getClientId();
        }

        if (!userRepository.existsById(effectiveClientId)) {
            throw new ResourceNotFoundException("User not found with id: " + effectiveClientId);
        }

        Project project = new Project();
        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setBudget(request.getBudget());
        project.setDeadline(request.getDeadline());
        project.setClientId(effectiveClientId);
        project.setStatus("OPEN");

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
            project.setCategoryId(category.getId());
            project.setCategory(category);
        }

        if (request.getSkillIds() != null && !request.getSkillIds().isEmpty()) {
            List<Skill> skills = skillRepository.findAllById(request.getSkillIds());
            project.setSkills(skills);
        }

        Project savedProject = projectRepository.save(project);
        return ProjectResponse.fromEntity(savedProject);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll()
                .stream()
                .map(ProjectResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<ProjectResponse> getProjectsPaginated(int page, int size, String sortBy, String sortDir) {
        return getProjectsPaginated(null, null, null, null, null, null, page, size, sortBy, sortDir);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProjectResponse> getProjectsPaginated(
            String keyword,
            Long categoryId,
            Long skillId,
            Double minBudget,
            Double maxBudget,
            String status,
            int page,
            int size,
            String sortBy,
            String sortDir) {

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

        if (minBudget != null && minBudget < 0) {
            throw new IllegalArgumentException("Minimum budget must not be negative");
        }
        if (maxBudget != null && maxBudget < 0) {
            throw new IllegalArgumentException("Maximum budget must not be negative");
        }
        if (minBudget != null && maxBudget != null && minBudget > maxBudget) {
            throw new IllegalArgumentException("Minimum budget cannot be greater than maximum budget");
        }

        User currentUser = null;
        try {
            currentUser = currentUserService.getCurrentUser();
        } catch (Exception ignored) {
            // Unauthenticated visitor browsing projects
        }
        boolean isAdmin = currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());

        String effectiveStatus = status;
        if (status == null || status.trim().isEmpty()) {
            effectiveStatus = "OPEN";
        } else {
            effectiveStatus = status.trim().toUpperCase();
            if (!VALID_STATUSES.contains(effectiveStatus)) {
                throw new IllegalArgumentException("Invalid status value: " + status);
            }
            if (!isAdmin && !"OPEN".equalsIgnoreCase(effectiveStatus)) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Non-admin users are only permitted to search for OPEN projects"
                );
            }
        }

        org.springframework.data.jpa.domain.Specification<Project> spec =
                com.smartfreelance.repository.ProjectSpecification.filterProjects(
                        keyword, categoryId, skillId, minBudget, maxBudget, effectiveStatus
                );

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<Project> projectPage = projectRepository.findAll(spec, pageable);

        return PageResponse.fromPage(projectPage, ProjectResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        return ProjectResponse.fromEntity(project);
    }

    @Transactional
    public ProjectResponse completeProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        User currentUser = currentUserService.getCurrentUser();
        if ("CLIENT".equalsIgnoreCase(currentUser.getRole()) && !project.getClientId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied: You do not own this project.");
        }

        if ("COMPLETED".equalsIgnoreCase(project.getStatus())) {
            return ProjectResponse.fromEntity(project);
        }

        if (milestoneRepository != null && paymentRepository != null) {
            List<Milestone> milestones = milestoneRepository.findByProjectId(id);
            if (milestones.isEmpty()) {
                throw new IllegalStateException("Project has no milestones to complete.");
            }

            boolean allMilestonesApproved = milestones.stream()
                    .allMatch(m -> "APPROVED".equalsIgnoreCase(m.getStatus()));
            if (!allMilestonesApproved) {
                throw new IllegalStateException("All milestones must be approved before completing the project.");
            }

            List<Payment> payments = paymentRepository.findByProjectId(id);
            boolean allMilestonesPaid = milestones.stream().allMatch(m ->
                    payments.stream().anyMatch(p -> Objects.equals(p.getMilestoneId(), m.getId()) && "COMPLETED".equalsIgnoreCase(p.getStatus()))
            );
            if (!allMilestonesPaid) {
                throw new IllegalStateException("All milestones must have settled payments before completing the project.");
            }
        }

        project.setStatus("COMPLETED");
        Project savedProject = projectRepository.save(project);

        if (notificationService != null) {
            notificationService.createNotification(
                    savedProject.getClientId(),
                    "Project Completed",
                    "Project '" + savedProject.getTitle() + "' has been successfully completed.",
                    "PROJECT"
            );
            if (savedProject.getFreelancerId() != null) {
                notificationService.createNotification(
                        savedProject.getFreelancerId(),
                        "Project Completed",
                        "Project '" + savedProject.getTitle() + "' has been marked as completed.",
                        "PROJECT"
                );
            }
        }

        return ProjectResponse.fromEntity(savedProject);
    }
}
