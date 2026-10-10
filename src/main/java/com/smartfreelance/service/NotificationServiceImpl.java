package com.smartfreelance.service;

import com.smartfreelance.dto.NotificationResponse;
import com.smartfreelance.dto.UnreadCountResponse;
import com.smartfreelance.entity.Notification;
import com.smartfreelance.entity.User;
import com.smartfreelance.exception.ResourceNotFoundException;
import com.smartfreelance.repository.NotificationRepository;
import com.smartfreelance.security.CurrentUserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final CurrentUserService currentUserService;

    public NotificationServiceImpl(NotificationRepository notificationRepository, CurrentUserService currentUserService) {
        this.notificationRepository = notificationRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    @Transactional
    public Notification createNotification(Long userId, String title, String message, String type) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications() {
        User currentUser = currentUserService.getCurrentUser();
        return notificationRepository.findByUserId(currentUser.getId())
                .stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications() {
        User currentUser = currentUserService.getCurrentUser();
        return notificationRepository.findByUserIdAndRead(currentUser.getId(), false)
                .stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount() {
        User currentUser = currentUserService.getCurrentUser();
        long count = notificationRepository.countByUserIdAndRead(currentUser.getId(), false);
        return new UnreadCountResponse(count);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        User currentUser = currentUserService.getCurrentUser();
        if (!notification.getUserId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied: You do not own this notification.");
        }

        notification.setRead(true);
        Notification savedNotification = notificationRepository.save(notification);
        return NotificationResponse.fromEntity(savedNotification);
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        User currentUser = currentUserService.getCurrentUser();
        List<Notification> unreadList = notificationRepository.findByUserIdAndRead(currentUser.getId(), false);
        for (Notification n : unreadList) {
            n.setRead(true);
        }
        notificationRepository.saveAll(unreadList);
    }

    @Override
    @Transactional
    public void deleteNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        User currentUser = currentUserService.getCurrentUser();
        if (!notification.getUserId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied: You do not own this notification.");
        }

        notificationRepository.delete(notification);
    }
}
