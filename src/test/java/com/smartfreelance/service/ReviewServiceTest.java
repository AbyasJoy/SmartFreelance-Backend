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
public class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ReviewService reviewService;

    private User clientUser;
    private User freelancerUser;
    private User unrelatedUser;
    private Project completedProject;
    private Project inProgressProject;

    @BeforeEach
    void setUp() {
        clientUser = new User(1L, "Client User", "client@example.com", "pass", "CLIENT", true);
        freelancerUser = new User(2L, "Freelancer User", "free@example.com", "pass", "FREELANCER", true);
        unrelatedUser = new User(3L, "Unrelated User", "unrelated@example.com", "pass", "FREELANCER", true);

        completedProject = new Project();
        completedProject.setId(10L);
        completedProject.setTitle("Completed Project");
        completedProject.setClientId(1L);
        completedProject.setFreelancerId(2L);
        completedProject.setStatus("COMPLETED");

        inProgressProject = new Project();
        inProgressProject.setId(11L);
        inProgressProject.setTitle("In Progress Project");
        inProgressProject.setClientId(1L);
        inProgressProject.setFreelancerId(2L);
        inProgressProject.setStatus("IN_PROGRESS");
    }

    @Test
    void testSubmitReview_ClientToFreelancer_Success() {
        when(currentUserService.getCurrentUser()).thenReturn(clientUser);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(completedProject));
        when(reviewRepository.existsByProjectIdAndReviewerId(10L, 1L)).thenReturn(false);

        Review savedReview = new Review(100L, 10L, 1L, 2L, 5, "Great work!");
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        ReviewRequest request = new ReviewRequest(10L, 5, "Great work!");
        ReviewResponse response = reviewService.submitReview(request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(10L, response.getProjectId());
        assertEquals(1L, response.getReviewerId());
        assertEquals(2L, response.getRevieweeId());
        assertEquals(5, response.getRating());
        assertEquals("Great work!", response.getComment());
    }

    @Test
    void testSubmitReview_FreelancerToClient_Success() {
        when(currentUserService.getCurrentUser()).thenReturn(freelancerUser);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(completedProject));
        when(reviewRepository.existsByProjectIdAndReviewerId(10L, 2L)).thenReturn(false);

        Review savedReview = new Review(101L, 10L, 2L, 1L, 5, "Great client!");
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        ReviewRequest request = new ReviewRequest(10L, 5, "Great client!");
        ReviewResponse response = reviewService.submitReview(request);

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals(10L, response.getProjectId());
        assertEquals(2L, response.getReviewerId());
        assertEquals(1L, response.getRevieweeId());
        assertEquals(5, response.getRating());
    }

    @Test
    void testSubmitReview_RatingBoundaries_Min1AndMax5() {
        when(currentUserService.getCurrentUser()).thenReturn(clientUser);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(completedProject));
        when(reviewRepository.existsByProjectIdAndReviewerId(10L, 1L)).thenReturn(false);

        Review savedReview1 = new Review(102L, 10L, 1L, 2L, 1, "Poor");
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview1);

        ReviewRequest request1 = new ReviewRequest(10L, 1, "Poor");
        ReviewResponse response1 = reviewService.submitReview(request1);
        assertEquals(1, response1.getRating());

        Review savedReview5 = new Review(103L, 10L, 1L, 2L, 5, "Excellent");
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview5);

        ReviewRequest request5 = new ReviewRequest(10L, 5, "Excellent");
        ReviewResponse response5 = reviewService.submitReview(request5);
        assertEquals(5, response5.getRating());
    }

    @Test
    void testSubmitReview_ProjectNotCompleted_ThrowsIllegalStateException() {
        when(currentUserService.getCurrentUser()).thenReturn(clientUser);
        when(projectRepository.findById(11L)).thenReturn(Optional.of(inProgressProject));

        ReviewRequest request = new ReviewRequest(11L, 5, "Premature review");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> reviewService.submitReview(request));
        assertTrue(ex.getMessage().contains("completed projects"));
    }

    @Test
    void testSubmitReview_UnrelatedUser_ThrowsAccessDeniedException() {
        when(currentUserService.getCurrentUser()).thenReturn(unrelatedUser);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(completedProject));

        ReviewRequest request = new ReviewRequest(10L, 4, "I wasn't on this project");

        assertThrows(AccessDeniedException.class, () -> reviewService.submitReview(request));
    }

    @Test
    void testSubmitReview_DuplicateReview_ThrowsIllegalStateException() {
        when(currentUserService.getCurrentUser()).thenReturn(clientUser);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(completedProject));
        when(reviewRepository.existsByProjectIdAndReviewerId(10L, 1L)).thenReturn(true);

        ReviewRequest request = new ReviewRequest(10L, 4, "Second review attempt");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> reviewService.submitReview(request));
        assertTrue(ex.getMessage().contains("already been submitted"));
    }

    @Test
    void testSubmitReview_BothPartiesReviewSameProject_Success() {
        // Client reviews freelancer
        when(currentUserService.getCurrentUser()).thenReturn(clientUser);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(completedProject));
        when(reviewRepository.existsByProjectIdAndReviewerId(10L, 1L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(new Review(201L, 10L, 1L, 2L, 5, "Client review"));

        ReviewResponse res1 = reviewService.submitReview(new ReviewRequest(10L, 5, "Client review"));
        assertNotNull(res1);
        assertEquals(1L, res1.getReviewerId());
        assertEquals(2L, res1.getRevieweeId());

        // Freelancer reviews client
        when(currentUserService.getCurrentUser()).thenReturn(freelancerUser);
        when(reviewRepository.existsByProjectIdAndReviewerId(10L, 2L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(new Review(202L, 10L, 2L, 1L, 4, "Freelancer review"));

        ReviewResponse res2 = reviewService.submitReview(new ReviewRequest(10L, 4, "Freelancer review"));
        assertNotNull(res2);
        assertEquals(2L, res2.getReviewerId());
        assertEquals(1L, res2.getRevieweeId());
    }

    @Test
    void testGetReviewsByProject_Success() {
        when(projectRepository.existsById(10L)).thenReturn(true);
        Review r1 = new Review(1L, 10L, 1L, 2L, 5, "Good");
        Review r2 = new Review(2L, 10L, 2L, 1L, 4, "Thanks");
        when(reviewRepository.findByProjectId(10L)).thenReturn(List.of(r1, r2));

        List<ReviewResponse> results = reviewService.getReviewsByProject(10L);

        assertEquals(2, results.size());
        assertEquals(5, results.get(0).getRating());
        assertEquals(4, results.get(1).getRating());
    }

    @Test
    void testGetReviewsByProject_NonExistentProject_ThrowsResourceNotFoundException() {
        when(projectRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> reviewService.getReviewsByProject(999L));
    }

    @Test
    void testGetReviewsByUser_Success() {
        when(userRepository.existsById(2L)).thenReturn(true);
        Review r1 = new Review(1L, 10L, 1L, 2L, 5, "Excellent freelancer");
        Review r2 = new Review(3L, 12L, 4L, 2L, 4, "Solid developer");
        when(reviewRepository.findByRevieweeId(2L)).thenReturn(List.of(r1, r2));

        List<ReviewResponse> results = reviewService.getReviewsByUser(2L);

        assertEquals(2, results.size());
        assertEquals(2L, results.get(0).getRevieweeId());
    }

    @Test
    void testGetReviewsByUser_NonExistentUser_ThrowsResourceNotFoundException() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> reviewService.getReviewsByUser(999L));
    }

    @Test
    void testGetUserRatingSummary_CalculatesAverageAndCount() {
        when(userRepository.existsById(2L)).thenReturn(true);
        Review r1 = new Review(1L, 10L, 1L, 2L, 5, "Top notch");
        Review r2 = new Review(2L, 12L, 4L, 2L, 4, "Very good");
        when(reviewRepository.findByRevieweeId(2L)).thenReturn(List.of(r1, r2));

        UserRatingSummaryResponse summary = reviewService.getUserRatingSummary(2L);

        assertNotNull(summary);
        assertEquals(2L, summary.getUserId());
        assertEquals(4.5, summary.getAverageRating());
        assertEquals(2L, summary.getTotalReviews());
    }

    @Test
    void testGetUserRatingSummary_UserWithNoReviews_ReturnsZeroCountAndAverage() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(reviewRepository.findByRevieweeId(2L)).thenReturn(List.of());

        UserRatingSummaryResponse summary = reviewService.getUserRatingSummary(2L);

        assertNotNull(summary);
        assertEquals(2L, summary.getUserId());
        assertEquals(0.0, summary.getAverageRating());
        assertEquals(0L, summary.getTotalReviews());
    }

    @Test
    void testGetUserRatingSummary_NonExistentUser_ThrowsResourceNotFoundException() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> reviewService.getUserRatingSummary(999L));
    }
}
