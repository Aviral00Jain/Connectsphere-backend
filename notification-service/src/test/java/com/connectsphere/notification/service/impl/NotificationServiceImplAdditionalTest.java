package com.connectsphere.notification.service.impl;

import com.connectsphere.notification.dto.CreateNotificationRequest;
import com.connectsphere.notification.entity.Notification;
import com.connectsphere.notification.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplAdditionalTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ObjectProvider<JavaMailSender> mailSenderProvider;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void createNotificationShouldSkipEmailWhenSenderIsMissing() {
        ReflectionTestUtils.setField(notificationService, "emailEnabled", true);

        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setReceiverId(5L);
        request.setType("FOLLOW");
        request.setMessage("Someone followed you");

        Notification saved = Notification.builder()
                .id(10L)
                .receiverId(5L)
                .type("FOLLOW")
                .message("Someone followed you")
                .isRead(false)
                .build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(saved);
        when(mailSenderProvider.getIfAvailable()).thenReturn(null);

        assertEquals(10L, notificationService.createNotification(request).getId());
        verify(mailSenderProvider).getIfAvailable();
    }
}
