package com.smartfreelance.service;

import com.smartfreelance.dto.NotificationResponse;
import com.smartfreelance.dto.UnreadCountResponse;
import com.smartfreelance.entity.Notification;

import java.util.List;

public interface NotificationService {

    Notification createNotification(Long userId, String title, String message, String type);

    List<NotificationResponse> getUserNotifications();

    List<NotificationResponse> getUnreadNotifications();

    UnreadCountResponse getUnreadCount();

    NotificationResponse markAsRead(Long id);

    void markAllAsRead();

    void deleteNotification(Long id);
}
