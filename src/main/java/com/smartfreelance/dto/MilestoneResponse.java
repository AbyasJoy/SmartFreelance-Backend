package com.smartfreelance.dto;

import com.smartfreelance.entity.Milestone;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MilestoneResponse {

    private Long id;
    private Long projectId;
    private String title;
    private String description;
    private BigDecimal amount;
    private LocalDate dueDate;
    private String status;
    private String submissionNotes;

    public MilestoneResponse() {
    }

    public MilestoneResponse(Long id, Long projectId, String title, String description, BigDecimal amount, LocalDate dueDate, String status, String submissionNotes) {
        this.id = id;
        this.projectId = projectId;
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.dueDate = dueDate;
        this.status = status;
        this.submissionNotes = submissionNotes;
    }

    public static MilestoneResponse fromEntity(Milestone milestone) {
        if (milestone == null) {
            return null;
        }

        BigDecimal amt = null;
        if (milestone.getAmount() != null && !milestone.getAmount().isBlank()) {
            try {
                amt = new BigDecimal(milestone.getAmount());
            } catch (Exception e) {
                amt = null;
            }
        }

        LocalDate date = null;
        if (milestone.getDueDate() != null && !milestone.getDueDate().isBlank()) {
            try {
                date = LocalDate.parse(milestone.getDueDate());
            } catch (Exception e) {
                date = null;
            }
        }

        return new MilestoneResponse(
                milestone.getId(),
                milestone.getProjectId(),
                milestone.getTitle(),
                milestone.getDescription(),
                amt,
                date,
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
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
