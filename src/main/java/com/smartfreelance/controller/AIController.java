package com.smartfreelance.controller;

import com.smartfreelance.dto.AIChatRequest;
import com.smartfreelance.dto.AIChatResponse;
import com.smartfreelance.service.AIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI Assistant", description = "Gemini AI Chatbot and RAG Knowledge Retrieval Endpoints")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Send a message to the AI Assistant", description = "Retrieves relevant SmartFreelance platform knowledge and generates role-tailored assistance via Gemini AI.")
    @ApiResponse(responseCode = "200", description = "AI chat response generated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid chat request or message validation failure")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT token required")
    public ResponseEntity<AIChatResponse> chat(@Valid @RequestBody AIChatRequest request) {
        AIChatResponse response = aiService.chat(request);
        return ResponseEntity.ok(response);
    }
}
