package com.smartfreelance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AIChatRequest {

    @NotBlank(message = "Message is required")
    @Size(min = 1, max = 2000, message = "Message must not exceed 2000 characters")
    private String message;

    private String conversationContext;

    public AIChatRequest() {
    }

    public AIChatRequest(String message) {
        this.message = message;
    }

    public AIChatRequest(String message, String conversationContext) {
        this.message = message;
        this.conversationContext = conversationContext;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getConversationContext() {
        return conversationContext;
    }

    public void setConversationContext(String conversationContext) {
        this.conversationContext = conversationContext;
    }
}
