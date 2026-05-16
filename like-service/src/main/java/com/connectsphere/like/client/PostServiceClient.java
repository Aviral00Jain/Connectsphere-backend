package com.connectsphere.like.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class PostServiceClient {

    private final RestTemplate restTemplate;

    public void incrementLikeCount(Long postId) {
        restTemplate.put("http://POST-SERVICE/posts/" + postId + "/increment-like", null);
    }

    public void decrementLikeCount(Long postId) {
        restTemplate.put("http://POST-SERVICE/posts/" + postId + "/decrement-like", null);
    }
}