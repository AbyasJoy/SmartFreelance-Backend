package com.smartfreelance.controller;

import com.smartfreelance.dto.ReviewRequest;
import com.smartfreelance.dto.ReviewResponse;
import com.smartfreelance.dto.UserRatingSummaryResponse;
import com.smartfreelance.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Reviews & Ratings", description = "Endpoints for submitting and querying project ratings and client/freelancer feedback")
@SecurityRequirement(name = "bearerAuth")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Operation(summary = "Submit review", description = "Submits a rating and feedback review for a completed project.")
    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'FREELANCER', 'ADMIN')")
    public ResponseEntity<ReviewResponse> submitReview(@Valid @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.submitReview(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get reviews for project")
    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'FREELANCER', 'ADMIN')")
    public ResponseEntity<List<ReviewResponse>> getReviewsByProject(@PathVariable Long projectId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByProject(projectId);
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "Get reviews for user")
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'FREELANCER', 'ADMIN')")
    public ResponseEntity<List<ReviewResponse>> getReviewsByUser(@PathVariable Long userId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByUser(userId);
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "Get user rating summary", description = "Calculates average rating score and total review count for a user.")
    @GetMapping({"/user/{userId}/summary", "/summary/user/{userId}"})
    @PreAuthorize("hasAnyRole('CLIENT', 'FREELANCER', 'ADMIN')")
    public ResponseEntity<UserRatingSummaryResponse> getUserRatingSummary(@PathVariable Long userId) {
        UserRatingSummaryResponse summary = reviewService.getUserRatingSummary(userId);
        return ResponseEntity.ok(summary);
    }
}
