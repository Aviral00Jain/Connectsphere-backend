package com.connectsphere.follow.messaging;

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

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(notificationExchange, true, false);
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
