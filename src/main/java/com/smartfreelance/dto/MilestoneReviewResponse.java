package com.smartfreelance.dto;

import com.smartfreelance.entity.Milestone;

public class MilestoneReviewResponse {

    private Long id;
    private Long projectId;
    private String title;
    private String status;
    private String submissionNotes;
    private String reviewComment;

    public MilestoneReviewResponse() {
    }

    public MilestoneReviewResponse(Long id, Long projectId, String title, String status, String submissionNotes, String reviewComment) {
        this.id = id;
        this.projectId = projectId;
        this.title = title;
        this.status = status;
        this.submissionNotes = submissionNotes;
        this.reviewComment = reviewComment;
    }

    public static MilestoneReviewResponse fromEntity(Milestone milestone) {
        if (milestone == null) {
            return null;
        }
        return new MilestoneReviewResponse(
                milestone.getId(),
                milestone.getProjectId(),
                milestone.getTitle(),
                milestone.getStatus(),
                milestone.getSubmissionNotes(),
                milestone.getReviewComment()
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSubmissionNotes() {
        return submissionNotes;
    }

    public void setSubmissionNotes(String submissionNotes) {
        this.submissionNotes = submissionNotes;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }
}
