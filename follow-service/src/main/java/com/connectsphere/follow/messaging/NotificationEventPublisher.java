package com.connectsphere.follow.messaging;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class NotificationEventPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${connectsphere.rabbitmq.notification-exchange:connectsphere.notifications.exchange}")
    private String notificationExchange;

    @Value("${connectsphere.rabbitmq.new-follower-routing-key:notification.follow.created}")
    private String newFollowerRoutingKey;

    public void publishNewFollower(Long followerId, Long followingId) {
        NewFollowerEvent event = new NewFollowerEvent(followerId, followingId, LocalDateTime.now());
        try {
            rabbitTemplate.convertAndSend(notificationExchange, newFollowerRoutingKey, event);
        } catch (AmqpException ex) {
            LOGGER.warn("Failed to publish new follower event for follower {} and following {}: {}",
                    followerId, followingId, ex.getMessage());
        }
    }
}
