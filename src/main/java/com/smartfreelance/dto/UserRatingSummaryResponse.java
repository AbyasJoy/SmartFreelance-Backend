package com.smartfreelance.dto;

public class UserRatingSummaryResponse {

    private Long userId;
    private Double averageRating;
    private Long totalReviews;

    public UserRatingSummaryResponse() {
    }

    public UserRatingSummaryResponse(Long userId, Double averageRating, Long totalReviews) {
        this.userId = userId;
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Long getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Long totalReviews) {
        this.totalReviews = totalReviews;
    }
}
