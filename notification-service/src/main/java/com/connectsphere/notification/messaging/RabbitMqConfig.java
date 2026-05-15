package com.connectsphere.notification.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMqConfig {

    @Value("${connectsphere.rabbitmq.notification-exchange:connectsphere.notifications.exchange}")
    private String notificationExchange;

    @Value("${connectsphere.rabbitmq.new-follower-queue:connectsphere.notifications.follow-created}")
    private String newFollowerQueue;

    @Value("${connectsphere.rabbitmq.new-follower-routing-key:notification.follow.created}")
    private String newFollowerRoutingKey;

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(notificationExchange, true, false);
    }

    @Bean
    public Queue newFollowerQueue() {
        return new Queue(newFollowerQueue, true);
    }

    @Bean
    public Binding newFollowerBinding(Queue newFollowerQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(newFollowerQueue).to(notificationExchange).with(newFollowerRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setTrustedPackages("com.connectsphere");
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put("newFollowerEvent", NewFollowerEvent.class);
        classMapper.setIdClassMapping(idClassMapping);
        converter.setClassMapper(classMapper);
        return converter;
    }
}
