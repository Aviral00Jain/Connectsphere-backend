package com.connectsphere.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r.path("/api/v1/auth/**")
                        .uri("lb://AUTH-SERVICE"))
                .route("post-service", r -> r.path("/api/v1/posts/**")
                        .uri("lb://POST-SERVICE"))
                .route("comment-service", r -> r.path("/api/v1/comments/**")
                        .uri("lb://COMMENT-SERVICE"))
                .route("like-service", r -> r.path("/api/v1/likes/**")
                        .uri("lb://LIKE-SERVICE"))
                .route("follow-service", r -> r.path("/api/v1/follows/**")
                        .uri("lb://FOLLOW-SERVICE"))
                .route("notification-service", r -> r.path("/api/v1/notifications/**")
                        .uri("lb://NOTIFICATION-SERVICE"))
                .route("media-service", r -> r.path("/api/v1/media/**", "/api/v1/stories/**")
                        .uri("lb://MEDIA-SERVICE"))
                .route("search-service", r -> r.path("/api/v1/search/**", "/api/v1/hashtags/**")
                        .uri("lb://SEARCH-SERVICE"))
                .build();
    }
}