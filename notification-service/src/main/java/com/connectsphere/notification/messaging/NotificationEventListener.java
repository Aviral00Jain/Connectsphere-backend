package com.connectsphere.notification.messaging;

import com.connectsphere.notification.dto.CreateNotificationRequest;
import com.connectsphere.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = "${connectsphere.rabbitmq.new-follower-queue:connectsphere.notifications.follow-created}")
    public void handleNewFollower(NewFollowerEvent event) {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setReceiverId(event.getFollowingId());
        request.setActorId(event.getFollowerId());
        request.setType("FOLLOW");
        request.setMessage("You have a new follower");
        request.setTargetId(event.getFollowerId());
        request.setTargetType("USER");
        request.setDeepLinkUrl("/profile/" + event.getFollowerId());
        notificationService.createNotification(request);
    }
}
