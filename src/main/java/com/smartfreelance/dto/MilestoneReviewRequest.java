package com.smartfreelance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MilestoneReviewRequest {

    private Long clientId;

    @NotBlank(message = "Action is required")
    private String action;

    private String comment;

    public MilestoneReviewRequest() {
    }

    public MilestoneReviewRequest(String action, String comment) {
        this.action = action;
        this.comment = comment;
    }

    public MilestoneReviewRequest(Long clientId, String action, String comment) {
        this.clientId = clientId;
        this.action = action;
        this.comment = comment;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
