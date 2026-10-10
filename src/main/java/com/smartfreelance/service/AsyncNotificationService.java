package com.smartfreelance.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class AsyncNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncNotificationService.class);

    @Async("asyncExecutor")
    public CompletableFuture<String> logNotificationSideEffectAsync(Long userId, String title, String type) {
        String threadName = Thread.currentThread().getName();
        logger.info("[Async-Notification] Background task executed on thread: {} for user: {}, event: '{}', type: {}",
                threadName, userId, title, type);
        return CompletableFuture.completedFuture(threadName);
    }
}
