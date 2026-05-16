package com.connectsphere.notification.service.impl;

import com.connectsphere.notification.dto.BulkNotificationRequest;
import com.connectsphere.notification.dto.CreateNotificationRequest;
import com.connectsphere.notification.dto.NotificationResponse;
import com.connectsphere.notification.entity.Notification;
import com.connectsphere.notification.exception.ResourceNotFoundException;
import com.connectsphere.notification.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ObjectProvider<JavaMailSender> mailSenderProvider;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void createNotificationShouldDefaultToUnread() {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setReceiverId(5L);
        request.setMessage("New follower");

        Notification saved = Notification.builder()
                .id(1L)
                .receiverId(5L)
                .message("New follower")
                .isRead(false)
                .build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

        NotificationResponse response = notificationService.createNotification(request);

        assertEquals(1L, response.getId());
        assertFalse(response.isRead());
        verify(mailSenderProvider, never()).getIfAvailable();
    }

    @Test
    void getNotificationsByReceiverIdShouldMapRepositoryRows() {
        when(notificationRepository.findByReceiverIdOrderByCreatedAtDesc(5L)).thenReturn(List.of(
                Notification.builder().id(1L).receiverId(5L).message("a").isRead(false).build()
        ));

        List<NotificationResponse> responses = notificationService.getNotificationsByReceiverId(5L);

        assertEquals(1, responses.size());
        assertEquals("a", responses.get(0).getMessage());
    }

    @Test
    void markAsReadShouldThrowWhenNotificationMissing() {
        when(notificationRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notificationService.markAsRead(2L));
    }

    @Test
    void markAllAsReadShouldPersistUpdatedNotifications() {
        Notification first = Notification.builder().id(1L).receiverId(9L).isRead(false).build();
        Notification second = Notification.builder().id(2L).receiverId(9L).isRead(false).build();

        when(notificationRepository.findByReceiverIdOrderByCreatedAtDesc(9L)).thenReturn(List.of(first, second));
        when(notificationRepository.saveAll(List.of(first, second))).thenReturn(List.of(first, second));

        List<NotificationResponse> responses = notificationService.markAllAsRead(9L);

        assertEquals(2, responses.size());
        assertEquals(true, first.isRead());
        assertEquals(true, second.isRead());
    }

    @Test
    void getUnreadCountShouldDelegateToRepository() {
        when(notificationRepository.countByReceiverIdAndIsReadFalse(7L)).thenReturn(3L);

        assertEquals(3L, notificationService.getUnreadCount(7L));
    }

    @Test
    void deleteNotificationShouldRemoveExistingNotification() {
        Notification notification = Notification.builder().id(4L).build();
        when(notificationRepository.findById(4L)).thenReturn(Optional.of(notification));

        notificationService.deleteNotification(4L);

        verify(notificationRepository).delete(notification);
    }

    @Test
    void sendBulkNotificationShouldCreateOnePerReceiver() {
        BulkNotificationRequest request = new BulkNotificationRequest();
        request.setReceiverIds(List.of(1L, 2L));
        request.setActorId(99L);
        request.setType("SYSTEM");
        request.setMessage("Maintenance tonight");

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<NotificationResponse> responses = notificationService.sendBulkNotification(request);

        assertEquals(2, responses.size());
        assertEquals(List.of(1L, 2L), responses.stream().map(NotificationResponse::getReceiverId).toList());
    }
}
