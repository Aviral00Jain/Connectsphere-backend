package com.connectsphere.notification.service;

import com.connectsphere.notification.dto.BulkNotificationRequest;
import com.connectsphere.notification.dto.CreateNotificationRequest;
import com.connectsphere.notification.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {

    NotificationResponse createNotification(CreateNotificationRequest request);

    List<NotificationResponse> getNotificationsByReceiverId(Long receiverId);

    NotificationResponse markAsRead(Long id);

    List<NotificationResponse> markAllAsRead(Long receiverId);

    long getUnreadCount(Long receiverId);

    void deleteNotification(Long id);

    List<NotificationResponse> sendBulkNotification(BulkNotificationRequest request);
}
