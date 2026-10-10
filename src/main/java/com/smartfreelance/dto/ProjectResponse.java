package com.smartfreelance.dto;

import com.smartfreelance.entity.Project;
import com.smartfreelance.entity.Skill;

import java.util.List;
import java.util.stream.Collectors;

public class ProjectResponse {

    private Long id;
    private String title;
    private String description;
    private String budget;
    private String deadline;
    private String status;
    private Long clientId;
    private Long freelancerId;
    private Long categoryId;
    private String categoryName;
    private List<Long> skillIds;
    private List<String> skillNames;

    public ProjectResponse() {
    }

    public ProjectResponse(Long id, String title, String description, String budget, String deadline, String status, Long clientId, Long freelancerId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.budget = budget;
        this.deadline = deadline;
        this.status = status;
        this.clientId = clientId;
        this.freelancerId = freelancerId;
    }

    public static ProjectResponse fromEntity(Project project) {
        ProjectResponse response = new ProjectResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getBudget(),
                project.getDeadline(),
                project.getStatus(),
                project.getClientId(),
                project.getFreelancerId()
        );
        response.setCategoryId(project.getCategoryId());
        if (project.getCategory() != null) {
            response.setCategoryName(project.getCategory().getName());
        }
        if (project.getSkills() != null && !project.getSkills().isEmpty()) {
            response.setSkillIds(project.getSkills().stream().map(Skill::getId).collect(Collectors.toList()));
            response.setSkillNames(project.getSkills().stream().map(Skill::getName).collect(Collectors.toList()));
        }
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getFreelancerId() {
        return freelancerId;
    }

    public void setFreelancerId(Long freelancerId) {
        this.freelancerId = freelancerId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public List<Long> getSkillIds() {
        return skillIds;
    }

    public void setSkillIds(List<Long> skillIds) {
        this.skillIds = skillIds;
    }

    public List<String> getSkillNames() {
        return skillNames;
    }

    public void setSkillNames(List<String> skillNames) {
        this.skillNames = skillNames;
    }
}
