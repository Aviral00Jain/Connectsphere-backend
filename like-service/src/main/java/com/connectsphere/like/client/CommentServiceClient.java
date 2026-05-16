package com.connectsphere.like.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class CommentServiceClient {

    private final RestTemplate restTemplate;

    public void incrementLikeCount(Long commentId) {
        restTemplate.put("http://COMMENT-SERVICE/comments/" + commentId + "/increment-like", null);
    }

    public void decrementLikeCount(Long commentId) {
        restTemplate.put("http://COMMENT-SERVICE/comments/" + commentId + "/decrement-like", null);
    }
}