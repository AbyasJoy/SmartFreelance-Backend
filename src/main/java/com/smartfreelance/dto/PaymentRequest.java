package com.smartfreelance.dto;

import jakarta.validation.constraints.NotNull;

public class PaymentRequest {

    @NotNull(message = "Milestone ID is required")
    private Long milestoneId;

    public PaymentRequest() {
    }

    public PaymentRequest(Long milestoneId) {
        this.milestoneId = milestoneId;
    }

    public Long getMilestoneId() {
        return milestoneId;
    }

    public void setMilestoneId(Long milestoneId) {
        this.milestoneId = milestoneId;
    }
}
