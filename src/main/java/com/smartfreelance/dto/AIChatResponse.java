package com.smartfreelance.dto;

import java.time.LocalDateTime;

public class AIChatResponse {

    private String reply;
    private String status;
    private String model;
    private String timestamp;

    public AIChatResponse() {
        this.timestamp = LocalDateTime.now().toString();
    }

    public AIChatResponse(String reply, String status, String model) {
        this.reply = reply;
        this.status = status;
        this.model = model;
        this.timestamp = LocalDateTime.now().toString();
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
