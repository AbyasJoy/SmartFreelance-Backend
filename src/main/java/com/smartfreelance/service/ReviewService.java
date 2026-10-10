package com.smartfreelance.service;

import com.smartfreelance.dto.ReviewRequest;
import com.smartfreelance.dto.ReviewResponse;
import com.smartfreelance.dto.UserRatingSummaryResponse;
import com.smartfreelance.entity.Project;
import com.smartfreelance.entity.Review;
import com.smartfreelance.entity.User;
import com.smartfreelance.exception.ResourceNotFoundException;
import com.smartfreelance.repository.ProjectRepository;
import com.smartfreelance.repository.ReviewRepository;
import com.smartfreelance.repository.UserRepository;
import com.smartfreelance.security.CurrentUserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;

    public ReviewService(ReviewRepository reviewRepository,
                         ProjectRepository projectRepository,
                         UserRepository userRepository,
                         CurrentUserService currentUserService,
                         NotificationService notificationService) {
        this.reviewRepository = reviewRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.notificationService = notificationService;
    }

    @Transactional
    public ReviewResponse submitReview(ReviewRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + request.getProjectId()));

        if (!"COMPLETED".equalsIgnoreCase(project.getStatus())) {
            throw new IllegalStateException("Reviews can only be submitted for completed projects.");
        }

        Long currentUserId = currentUser.getId();
        boolean isClient = Objects.equals(project.getClientId(), currentUserId);
        boolean isFreelancer = Objects.equals(project.getFreelancerId(), currentUserId);

        if (!isClient && !isFreelancer) {
            throw new AccessDeniedException("Access denied: You are not a participant in this project.");
        }

        Long revieweeId;
        if (isClient) {
            if (project.getFreelancerId() == null) {
                throw new IllegalStateException("Project does not have a selected freelancer to review.");
            }
            revieweeId = project.getFreelancerId();
        } else {
            revieweeId = project.getClientId();
        }

        if (reviewRepository.existsByProjectIdAndReviewerId(project.getId(), currentUserId)) {
            throw new IllegalStateException("Review has already been submitted by this user for project: " + project.getId());
        }

        Review review = new Review();
        review.setProjectId(project.getId());
        review.setReviewerId(currentUserId);
        review.setRevieweeId(revieweeId);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review savedReview = reviewRepository.save(review);

        notificationService.createNotification(
                revieweeId,
                "New Review Received",
                "You received a new review with rating " + request.getRating() + "/5.",
                "REVIEW"
        );

        return ReviewResponse.fromEntity(savedReview);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }
        List<Review> reviews = reviewRepository.findByProjectId(projectId);
        return reviews.stream().map(ReviewResponse::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        List<Review> reviews = reviewRepository.findByRevieweeId(userId);
        return reviews.stream().map(ReviewResponse::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserRatingSummaryResponse getUserRatingSummary(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        List<Review> reviews = reviewRepository.findByRevieweeId(userId);
        if (reviews.isEmpty()) {
            return new UserRatingSummaryResponse(userId, 0.0, 0L);
        }

        double average = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        double roundedAverage = Math.round(average * 100.0) / 100.0;

        return new UserRatingSummaryResponse(userId, roundedAverage, (long) reviews.size());
    }
}
