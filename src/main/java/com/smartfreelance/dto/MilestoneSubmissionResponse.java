package com.smartfreelance.dto;

import com.smartfreelance.entity.Milestone;

public class MilestoneSubmissionResponse {

    private Long id;
    private Long projectId;
    private String title;
    private String status;
    private String submissionNotes;

    public MilestoneSubmissionResponse() {
    }

    public MilestoneSubmissionResponse(Long id, Long projectId, String title, String status, String submissionNotes) {
        this.id = id;
        this.projectId = projectId;
        this.title = title;
        this.status = status;
        this.submissionNotes = submissionNotes;
    }

    public static MilestoneSubmissionResponse fromEntity(Milestone milestone) {
        if (milestone == null) {
            return null;
        }
        return new MilestoneSubmissionResponse(
                milestone.getId(),
                milestone.getProjectId(),
                milestone.getTitle(),
                milestone.getStatus(),
                milestone.getSubmissionNotes()
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
}
