package com.connectsphere.notification.controller;

import com.connectsphere.notification.dto.BulkNotificationRequest;
import com.connectsphere.notification.dto.CreateNotificationRequest;
import com.connectsphere.notification.dto.NotificationResponse;
import com.connectsphere.notification.service.NotificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Validated
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public NotificationResponse createNotification(@Valid @RequestBody CreateNotificationRequest request) {
        return notificationService.createNotification(request);
    }

    @GetMapping("/receiver/{receiverId}")
    public List<NotificationResponse> getNotificationsByReceiverId(@PathVariable @Positive Long receiverId) {
        return notificationService.getNotificationsByReceiverId(receiverId);
    }

    @PutMapping("/{id}/read")
    public NotificationResponse markAsRead(@PathVariable @Positive Long id) {
        return notificationService.markAsRead(id);
    }

    @PutMapping("/receiver/{receiverId}/read-all")
    public List<NotificationResponse> markAllAsRead(@PathVariable @Positive Long receiverId) {
        return notificationService.markAllAsRead(receiverId);
    }

    @GetMapping("/receiver/{receiverId}/unread-count")
    public java.util.Map<String, Long> getUnreadCount(@PathVariable @Positive Long receiverId) {
        return java.util.Map.of("unreadCount", notificationService.getUnreadCount(receiverId));
    }

    @DeleteMapping("/{id}")
    public java.util.Map<String, String> deleteNotification(@PathVariable @Positive Long id) {
        notificationService.deleteNotification(id);
        return java.util.Map.of("message", "Notification deleted successfully");
    }

    @PostMapping("/bulk")
    public List<NotificationResponse> sendBulkNotification(@Valid @RequestBody BulkNotificationRequest request) {
        return notificationService.sendBulkNotification(request);
    }
}
