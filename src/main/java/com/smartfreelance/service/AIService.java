package com.smartfreelance.service;

import com.smartfreelance.dto.AIChatRequest;
import com.smartfreelance.dto.AIChatResponse;

public interface AIService {
    AIChatResponse chat(AIChatRequest request);
}
