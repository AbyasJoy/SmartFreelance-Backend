package com.smartfreelance.dto;

import com.smartfreelance.entity.Proposal;

public class ProposalResponse {

    private Long id;
    private Long projectId;
    private Long freelancerId;
    private String coverLetter;
    private String proposedPrice;
    private String estimatedDays;
    private String status;

    public ProposalResponse() {
    }

    public ProposalResponse(Long id, Long projectId, Long freelancerId, String coverLetter, String proposedPrice, String estimatedDays, String status) {
        this.id = id;
        this.projectId = projectId;
        this.freelancerId = freelancerId;
        this.coverLetter = coverLetter;
        this.proposedPrice = proposedPrice;
        this.estimatedDays = estimatedDays;
        this.status = status;
    }

    public static ProposalResponse fromEntity(Proposal proposal) {
        return new ProposalResponse(
                proposal.getId(),
                proposal.getProjectId(),
                proposal.getFreelancerId(),
                proposal.getCoverLetter(),
                proposal.getProposedPrice(),
                proposal.getEstimatedDays(),
                proposal.getStatus()
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
