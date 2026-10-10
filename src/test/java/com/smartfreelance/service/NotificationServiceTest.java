package com.smartfreelance.service;

import com.smartfreelance.dto.NotificationResponse;
import com.smartfreelance.dto.UnreadCountResponse;
import com.smartfreelance.entity.Notification;
import com.smartfreelance.entity.User;
import com.smartfreelance.exception.ResourceNotFoundException;
import com.smartfreelance.repository.NotificationRepository;
import com.smartfreelance.security.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User user1;
    private User user2;
    private Notification notification1;
    private Notification notification2;

    @BeforeEach
    void setUp() {
        user1 = new User(1L, "User One", "user1@example.com", "pass", "CLIENT", true);
        user2 = new User(2L, "User Two", "user2@example.com", "pass", "FREELANCER", true);

        notification1 = new Notification(10L, 1L, "New Proposal", "A new proposal received", "PROPOSAL", false);
        notification2 = new Notification(11L, 1L, "Payment Received", "Payment completed", "PAYMENT", true);
    }

    @Test
    void testCreateNotification_Success() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        Notification created = notificationService.createNotification(1L, "Title", "Message", "TYPE");

        assertNotNull(created);
        assertEquals(1L, created.getUserId());
        assertEquals("Title", created.getTitle());
        assertEquals("Message", created.getMessage());
        assertEquals("TYPE", created.getType());
        assertFalse(created.isRead());
    }

    @Test
    void testGetUserNotifications_Success() {
        when(currentUserService.getCurrentUser()).thenReturn(user1);
        when(notificationRepository.findByUserId(1L)).thenReturn(List.of(notification1, notification2));

        List<NotificationResponse> result = notificationService.getUserNotifications();

        assertEquals(2, result.size());
        assertEquals("New Proposal", result.get(0).getTitle());
        assertEquals("Payment Received", result.get(1).getTitle());
    }

    @Test
    void testGetUnreadNotifications_Success() {
        when(currentUserService.getCurrentUser()).thenReturn(user1);
        when(notificationRepository.findByUserIdAndRead(1L, false)).thenReturn(List.of(notification1));

        List<NotificationResponse> result = notificationService.getUnreadNotifications();

        assertEquals(1, result.size());
        assertFalse(result.get(0).isRead());
    }

    @Test
    void testGetUnreadCount_Success() {
        when(currentUserService.getCurrentUser()).thenReturn(user1);
        when(notificationRepository.countByUserIdAndRead(1L, false)).thenReturn(5L);

        UnreadCountResponse response = notificationService.getUnreadCount();

        assertEquals(5L, response.getCount());
        assertEquals(5L, response.getUnreadCount());
    }

    @Test
    void testMarkAsRead_Success() {
        when(currentUserService.getCurrentUser()).thenReturn(user1);
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification1));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationResponse response = notificationService.markAsRead(10L);

        assertNotNull(response);
        assertTrue(response.isRead());
    }

    @Test
    void testMarkAsRead_OtherUser_ThrowsAccessDeniedException() {
        when(currentUserService.getCurrentUser()).thenReturn(user2);
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification1));

        assertThrows(AccessDeniedException.class, () -> notificationService.markAsRead(10L));
    }

    @Test
    void testMarkAsRead_NonExistent_ThrowsResourceNotFoundException() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notificationService.markAsRead(999L));
    }

    @Test
    void testMarkAllAsRead_Success() {
        when(currentUserService.getCurrentUser()).thenReturn(user1);
        when(notificationRepository.findByUserIdAndRead(1L, false)).thenReturn(List.of(notification1));

        notificationService.markAllAsRead();

        assertTrue(notification1.isRead());
        verify(notificationRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testDeleteNotification_Success() {
        when(currentUserService.getCurrentUser()).thenReturn(user1);
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification1));

        notificationService.deleteNotification(10L);

        verify(notificationRepository, times(1)).delete(notification1);
    }

    @Test
    void testDeleteNotification_OtherUser_ThrowsAccessDeniedException() {
        when(currentUserService.getCurrentUser()).thenReturn(user2);
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification1));

        assertThrows(AccessDeniedException.class, () -> notificationService.deleteNotification(10L));
    }

    @Test
    void testDeleteNotification_NonExistent_ThrowsResourceNotFoundException() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notificationService.deleteNotification(999L));
    }
}
