package com.smartfreelance.service;

import com.smartfreelance.dto.PageResponse;
import com.smartfreelance.dto.ProposalRequest;
import com.smartfreelance.dto.ProposalResponse;
import com.smartfreelance.entity.Project;
import com.smartfreelance.entity.Proposal;
import com.smartfreelance.entity.User;
import com.smartfreelance.exception.ResourceNotFoundException;
import com.smartfreelance.repository.ProjectRepository;
import com.smartfreelance.repository.ProposalRepository;
import com.smartfreelance.repository.UserRepository;
import com.smartfreelance.security.CurrentUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProposalService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "projectId", "freelancerId", "proposedPrice", "estimatedDays", "status", "coverLetter"
    );

    private final ProposalRepository proposalRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;

    public ProposalService(ProposalRepository proposalRepository,
                           ProjectRepository projectRepository,
                           UserRepository userRepository,
                           CurrentUserService currentUserService,
                           NotificationService notificationService) {
        this.proposalRepository = proposalRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.notificationService = notificationService;
    }

    @Transactional
    public ProposalResponse createProposal(ProposalRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        Long effectiveFreelancerId = currentUser.getId();

        if ("ADMIN".equalsIgnoreCase(currentUser.getRole()) && request.getFreelancerId() != null) {
            effectiveFreelancerId = request.getFreelancerId();
        }

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + request.getProjectId()));

        if (!userRepository.existsById(effectiveFreelancerId)) {
            throw new ResourceNotFoundException("User not found with id: " + effectiveFreelancerId);
        }

        Proposal proposal = new Proposal();
        proposal.setProjectId(request.getProjectId());
        proposal.setFreelancerId(effectiveFreelancerId);
        proposal.setCoverLetter(request.getCoverLetter());
        proposal.setProposedPrice(request.getProposedPrice());
        proposal.setEstimatedDays(request.getEstimatedDays());
        proposal.setStatus("PENDING");

        Proposal savedProposal = proposalRepository.save(proposal);

        notificationService.createNotification(
                project.getClientId(),
                "New Proposal Received",
                "A new proposal was submitted for project: " + project.getTitle(),
                "PROPOSAL"
        );

        return ProposalResponse.fromEntity(savedProposal);
    }

    @Transactional(readOnly = true)
    public List<ProposalResponse> getProposalsByProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        User currentUser = currentUserService.getCurrentUser();
        if ("CLIENT".equalsIgnoreCase(currentUser.getRole()) && !project.getClientId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied: You do not own this project");
        }

        return proposalRepository.findByProjectId(projectId)
                .stream()
                .map(ProposalResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProposalResponse> getProposalsByFreelancer(Long freelancerId) {
        User currentUser = currentUserService.getCurrentUser();
        if ("FREELANCER".equalsIgnoreCase(currentUser.getRole()) && !currentUser.getId().equals(freelancerId)) {
            throw new AccessDeniedException("Access denied: You cannot view another freelancer's proposals");
        }

        return proposalRepository.findByFreelancerId(freelancerId)
                .stream()
                .map(ProposalResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<ProposalResponse> getProposalsPaginated(int page, int size, String sortBy, String sortDir) {
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
        Page<Proposal> proposalPage = proposalRepository.findAll(pageable);

        return PageResponse.fromPage(proposalPage, ProposalResponse::fromEntity);
    }

    @Transactional
    public ProposalResponse acceptProposal(Long proposalId) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found with id: " + proposalId));

        Project project = projectRepository.findById(proposal.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + proposal.getProjectId()));

        User currentUser = currentUserService.getCurrentUser();
        if ("CLIENT".equalsIgnoreCase(currentUser.getRole()) && !project.getClientId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied: You do not own this project");
        }

        if (!"OPEN".equalsIgnoreCase(project.getStatus())) {
            throw new RuntimeException("Proposal cannot be accepted because the project is not open.");
        }

        if (!"PENDING".equalsIgnoreCase(proposal.getStatus())) {
            throw new RuntimeException("Only pending proposals can be accepted.");
        }

        proposal.setStatus("ACCEPTED");
        project.setFreelancerId(proposal.getFreelancerId());
        project.setStatus("FREELANCER_SELECTED");

        List<Proposal> proposals = proposalRepository.findByProjectId(project.getId());
        for (Proposal p : proposals) {
            if (!p.getId().equals(proposal.getId()) && "PENDING".equalsIgnoreCase(p.getStatus())) {
                p.setStatus("REJECTED");
                notificationService.createNotification(
                        p.getFreelancerId(),
                        "Proposal Rejected",
                        "Your proposal for project '" + project.getTitle() + "' was rejected.",
                        "PROPOSAL"
                );
            }
        }

        proposalRepository.saveAll(proposals);
        projectRepository.save(project);

        notificationService.createNotification(
                proposal.getFreelancerId(),
                "Proposal Accepted",
                "Your proposal for project '" + project.getTitle() + "' has been accepted.",
                "PROPOSAL"
        );

        return ProposalResponse.fromEntity(proposal);
    }

    @Transactional
    public ProposalResponse rejectProposal(Long proposalId) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found with id: " + proposalId));

        Project project = projectRepository.findById(proposal.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + proposal.getProjectId()));

        User currentUser = currentUserService.getCurrentUser();
        if ("CLIENT".equalsIgnoreCase(currentUser.getRole()) && !project.getClientId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied: You do not own this project");
        }

        if (!"PENDING".equalsIgnoreCase(proposal.getStatus())) {
            throw new RuntimeException("Only pending proposals can be rejected.");
        }

        proposal.setStatus("REJECTED");
        Proposal savedProposal = proposalRepository.save(proposal);

        notificationService.createNotification(
                proposal.getFreelancerId(),
                "Proposal Rejected",
                "Your proposal for project '" + project.getTitle() + "' was rejected.",
                "PROPOSAL"
        );

        return ProposalResponse.fromEntity(savedProposal);
    }
}
