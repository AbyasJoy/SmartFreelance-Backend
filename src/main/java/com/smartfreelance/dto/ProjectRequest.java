package com.smartfreelance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class ProjectRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, message = "Title must be at least 3 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Budget is required")
    private String budget;

    @NotBlank(message = "Deadline is required")
    private String deadline;

    @NotNull(message = "Client ID is required")
    private Long clientId;

    private Long categoryId;

    private List<Long> skillIds;

    public ProjectRequest() {
    }

    public ProjectRequest(String title, String description, String budget, String deadline, Long clientId) {
        this.title = title;
        this.description = description;
        this.budget = budget;
        this.deadline = deadline;
        this.clientId = clientId;
    }

    public ProjectRequest(String title, String description, String budget, String deadline, Long clientId, Long categoryId, List<Long> skillIds) {
        this.title = title;
        this.description = description;
        this.budget = budget;
        this.deadline = deadline;
        this.clientId = clientId;
        this.categoryId = categoryId;
        this.skillIds = skillIds;
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

    public String getBudget() {
        return budget;
    }

    public void setBudget(String budget) {
        this.budget = budget;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public List<Long> getSkillIds() {
        return skillIds;
    }

    public void setSkillIds(List<Long> skillIds) {
        this.skillIds = skillIds;
    }
}
