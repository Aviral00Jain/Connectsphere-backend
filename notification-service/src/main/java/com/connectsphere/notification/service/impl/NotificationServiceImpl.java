package com.connectsphere.notification.service.impl;

import com.connectsphere.notification.dto.BulkNotificationRequest;
import com.connectsphere.notification.dto.CreateNotificationRequest;
import com.connectsphere.notification.dto.NotificationResponse;
import com.connectsphere.notification.entity.Notification;
import com.connectsphere.notification.exception.ResourceNotFoundException;
import com.connectsphere.notification.repository.NotificationRepository;
import com.connectsphere.notification.service.NotificationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${notification.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${notification.email.from:}")
    private String fromAddress;

    @Value("${notification.auth-service.base-url:http://localhost:8081/api/v1/auth}")
    private String authServiceBaseUrl;

    @Override
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        Notification notification = Notification.builder()
                .receiverId(request.getReceiverId())
                .actorId(request.getActorId())
                .type(request.getType())
                .message(request.getMessage())
                .targetId(request.getTargetId())
                .targetType(request.getTargetType())
                .deepLinkUrl(request.getDeepLinkUrl())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        Notification saved = notificationRepository.save(notification);
        maybeSendEmailAlert(saved);
        return mapToResponse(saved);
    }

    @Override
    public List<NotificationResponse> getNotificationsByReceiverId(Long receiverId) {
        return notificationRepository.findByReceiverIdOrderByCreatedAtDesc(receiverId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public NotificationResponse markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        notification.setRead(true);
        Notification updated = notificationRepository.save(notification);
        return mapToResponse(updated);
    }

    @Override
    public List<NotificationResponse> markAllAsRead(Long receiverId) {
        List<Notification> notifications = notificationRepository.findByReceiverIdOrderByCreatedAtDesc(receiverId);
        notifications.forEach(notification -> notification.setRead(true));
        return notificationRepository.saveAll(notifications)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public long getUnreadCount(Long receiverId) {
        return notificationRepository.countByReceiverIdAndIsReadFalse(receiverId);
    }

    @Override
    public void deleteNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        notificationRepository.delete(notification);
    }

    @Override
    public List<NotificationResponse> sendBulkNotification(BulkNotificationRequest request) {
        return request.getReceiverIds().stream()
                .map(receiverId -> createNotification(mapToCreateRequest(receiverId, request)))
                .toList();
    }

    private void maybeSendEmailAlert(Notification notification) {
        if (!emailEnabled || !shouldSendEmailAlert(notification.getType())) {
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            LOGGER.warn("Email notifications are enabled but no JavaMailSender bean is available.");
            return;
        }

        try {
            JsonNode profile = fetchUserProfile(notification.getReceiverId());
            String email = profile.path("email").asText("");
            String fullName = profile.path("fullName").asText("there");
            if (email.isBlank()) {
                LOGGER.warn("Skipping email alert because receiver {} has no email address.", notification.getReceiverId());
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            if (!fromAddress.isBlank()) {
                message.setFrom(fromAddress);
            }
            message.setTo(email);
            message.setSubject(buildEmailSubject(notification));
            message.setText(buildEmailBody(fullName, notification));
            mailSender.send(message);
        } catch (Exception ex) {
            LOGGER.warn("Failed to send email alert for notification {}: {}", notification.getId(), ex.getMessage());
        }
    }

    private boolean shouldSendEmailAlert(String type) {
        return "FOLLOW".equalsIgnoreCase(type) || "SYSTEM".equalsIgnoreCase(type);
    }

    private JsonNode fetchUserProfile(Long receiverId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(authServiceBaseUrl + "/profile/id/" + receiverId))
                .GET()
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Auth service profile lookup failed with status " + response.statusCode());
        }

        return objectMapper.readTree(response.body());
    }

    private String buildEmailSubject(Notification notification) {
        if ("FOLLOW".equalsIgnoreCase(notification.getType())) {
            return "ConnectSphere: You have a new follower";
        }
        if ("SYSTEM".equalsIgnoreCase(notification.getType())) {
            return "ConnectSphere: Platform update";
        }
        return "ConnectSphere notification";
    }

    private String buildEmailBody(String fullName, Notification notification) {
        StringBuilder builder = new StringBuilder();
        builder.append("Hi ").append(fullName).append(",\n\n");
        builder.append(notification.getMessage()).append("\n\n");
        if (notification.getDeepLinkUrl() != null && !notification.getDeepLinkUrl().isBlank()) {
            builder.append("Open: ").append(notification.getDeepLinkUrl()).append("\n\n");
        }
        builder.append("Thanks,\nConnectSphere");
        return builder.toString();
    }

    private CreateNotificationRequest mapToCreateRequest(Long receiverId, BulkNotificationRequest request) {
        CreateNotificationRequest createRequest = new CreateNotificationRequest();
        createRequest.setReceiverId(receiverId);
        createRequest.setActorId(request.getActorId());
        createRequest.setType(request.getType());
        createRequest.setMessage(request.getMessage());
        createRequest.setTargetId(request.getTargetId());
        createRequest.setTargetType(request.getTargetType());
        createRequest.setDeepLinkUrl(request.getDeepLinkUrl());
        return createRequest;
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .receiverId(notification.getReceiverId())
                .actorId(notification.getActorId())
                .type(notification.getType())
                .message(notification.getMessage())
                .targetId(notification.getTargetId())
                .targetType(notification.getTargetType())
                .deepLinkUrl(notification.getDeepLinkUrl())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
