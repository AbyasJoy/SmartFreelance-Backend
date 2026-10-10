package com.smartfreelance.dto;

import java.util.Map;

public class AdminDashboardResponse {

    private long totalUsers;
    private long activeUsers;
    private long totalClients;
    private long totalFreelancers;
    private long totalProjects;
    private Map<String, Long> projectsByStatus;
    private long totalProposals;
    private long totalCompletedProjects;
    private long totalPayments;
    private long totalCompletedPayments;
    private String totalSettledAmount;

    public AdminDashboardResponse() {
    }

    public AdminDashboardResponse(long totalUsers, long activeUsers, long totalClients, long totalFreelancers,
                                  long totalProjects, Map<String, Long> projectsByStatus, long totalProposals,
                                  long totalCompletedProjects, long totalPayments, long totalCompletedPayments,
                                  String totalSettledAmount) {
        this.totalUsers = totalUsers;
        this.activeUsers = activeUsers;
        this.totalClients = totalClients;
        this.totalFreelancers = totalFreelancers;
        this.totalProjects = totalProjects;
        this.projectsByStatus = projectsByStatus;
        this.totalProposals = totalProposals;
        this.totalCompletedProjects = totalCompletedProjects;
        this.totalPayments = totalPayments;
        this.totalCompletedPayments = totalCompletedPayments;
        this.totalSettledAmount = totalSettledAmount;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public long getTotalClients() {
        return totalClients;
    }

    public void setTotalClients(long totalClients) {
        this.totalClients = totalClients;
    }

    public long getTotalFreelancers() {
        return totalFreelancers;
    }

    public void setTotalFreelancers(long totalFreelancers) {
        this.totalFreelancers = totalFreelancers;
    }

    public long getTotalProjects() {
        return totalProjects;
    }

    public void setTotalProjects(long totalProjects) {
        this.totalProjects = totalProjects;
    }

    public Map<String, Long> getProjectsByStatus() {
        return projectsByStatus;
    }

    public void setProjectsByStatus(Map<String, Long> projectsByStatus) {
        this.projectsByStatus = projectsByStatus;
    }

    public long getTotalProposals() {
        return totalProposals;
    }

    public void setTotalProposals(long totalProposals) {
        this.totalProposals = totalProposals;
    }

    public long getTotalCompletedProjects() {
        return totalCompletedProjects;
    }

    public void setTotalCompletedProjects(long totalCompletedProjects) {
        this.totalCompletedProjects = totalCompletedProjects;
    }

    public long getTotalPayments() {
        return totalPayments;
    }

    public void setTotalPayments(long totalPayments) {
        this.totalPayments = totalPayments;
    }

    public long getTotalCompletedPayments() {
        return totalCompletedPayments;
    }

    public void setTotalCompletedPayments(long totalCompletedPayments) {
        this.totalCompletedPayments = totalCompletedPayments;
    }

    public String getTotalSettledAmount() {
        return totalSettledAmount;
    }

    public void setTotalSettledAmount(String totalSettledAmount) {
        this.totalSettledAmount = totalSettledAmount;
    }
}
