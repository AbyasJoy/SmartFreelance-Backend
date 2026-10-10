package com.smartfreelance.dto;

import com.smartfreelance.entity.Payment;

public class PaymentResponse {

    private Long id;
    private Long projectId;
    private Long milestoneId;
    private Long freelancerId;
    private String amount;
    private String status;
    private String paymentDate;
    private String transactionReference;

    private String projectTitle;
    private String milestoneTitle;

    public PaymentResponse() {
    }

    public PaymentResponse(Long id, Long projectId, Long milestoneId, Long freelancerId, String amount, String status, String paymentDate, String transactionReference) {
        this.id = id;
        this.projectId = projectId;
        this.milestoneId = milestoneId;
        this.freelancerId = freelancerId;
        this.amount = amount;
        this.status = status;
        this.paymentDate = paymentDate;
        this.transactionReference = transactionReference;
    }

    public static PaymentResponse fromEntity(Payment payment) {
        if (payment == null) {
            return null;
        }
        PaymentResponse response = new PaymentResponse(
                payment.getId(),
                payment.getProjectId(),
                payment.getMilestoneId(),
                payment.getFreelancerId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getPaymentDate(),
                payment.getTransactionReference()
        );
        if (payment.getProject() != null) {
            response.setProjectTitle(payment.getProject().getTitle());
        }
        if (payment.getMilestone() != null) {
            response.setMilestoneTitle(payment.getMilestone().getTitle());
        }
        return response;
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

    public Long getMilestoneId() {
        return milestoneId;
    }

    public void setMilestoneId(Long milestoneId) {
        this.milestoneId = milestoneId;
    }

    public Long getFreelancerId() {
        return freelancerId;
    }

    public void setFreelancerId(Long freelancerId) {
        this.freelancerId = freelancerId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public String getMilestoneTitle() {
        return milestoneTitle;
    }

    public void setMilestoneTitle(String milestoneTitle) {
        this.milestoneTitle = milestoneTitle;
    }
}
