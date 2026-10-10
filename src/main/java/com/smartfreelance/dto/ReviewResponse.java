package com.smartfreelance.dto;

import com.smartfreelance.entity.Review;

public class ReviewResponse {

    private Long id;
    private Long projectId;
    private Long reviewerId;
    private Long revieweeId;
    private Integer rating;
    private String comment;

    public ReviewResponse() {
    }

    public ReviewResponse(Long id, Long projectId, Long reviewerId, Long revieweeId, Integer rating, String comment) {
        this.id = id;
        this.projectId = projectId;
        this.reviewerId = reviewerId;
        this.revieweeId = revieweeId;
        this.rating = rating;
        this.comment = comment;
    }

    public static ReviewResponse fromEntity(Review review) {
        if (review == null) {
            return null;
        }
        return new ReviewResponse(
                review.getId(),
                review.getProjectId(),
                review.getReviewerId(),
                review.getRevieweeId(),
                review.getRating(),
                review.getComment()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public Long getRevieweeId() {
        return revieweeId;
    }

    public void setRevieweeId(Long revieweeId) {
        this.revieweeId = revieweeId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
