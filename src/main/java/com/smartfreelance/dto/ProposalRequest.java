package com.smartfreelance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProposalRequest {

    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotNull(message = "Freelancer ID is required")
    private Long freelancerId;

    @NotBlank(message = "Cover letter is required")
    private String coverLetter;

    @NotBlank(message = "Proposed price is required")
    private String proposedPrice;

    @NotBlank(message = "Estimated days is required")
    private String estimatedDays;

    public ProposalRequest() {
    }

    public ProposalRequest(Long projectId, Long freelancerId, String coverLetter, String proposedPrice, String estimatedDays) {
        this.projectId = projectId;
        this.freelancerId = freelancerId;
        this.coverLetter = coverLetter;
        this.proposedPrice = proposedPrice;
        this.estimatedDays = estimatedDays;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getFreelancerId() {
        return freelancerId;
    }

    public void setFreelancerId(Long freelancerId) {
        this.freelancerId = freelancerId;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }

    public String getProposedPrice() {
        return proposedPrice;
    }

    public void setProposedPrice(String proposedPrice) {
        this.proposedPrice = proposedPrice;
    }

    public String getEstimatedDays() {
        return estimatedDays;
    }

    public void setEstimatedDays(String estimatedDays) {
        this.estimatedDays = estimatedDays;
    }
}
