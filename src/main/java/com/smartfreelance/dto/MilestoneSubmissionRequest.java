package com.smartfreelance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MilestoneSubmissionRequest {

    @NotBlank(message = "Submission notes are required")
    @Size(min = 5, message = "Submission notes must be at least 5 characters")
    private String submissionNotes;

    public MilestoneSubmissionRequest() {
    }

    public MilestoneSubmissionRequest(String submissionNotes) {
        this.submissionNotes = submissionNotes;
    }

    public String getSubmissionNotes() {
        return submissionNotes;
    }

    public void setSubmissionNotes(String submissionNotes) {
        this.submissionNotes = submissionNotes;
    }
}
