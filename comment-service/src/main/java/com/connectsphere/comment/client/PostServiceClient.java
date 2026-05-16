package com.connectsphere.comment.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class PostServiceClient {

    private final RestTemplate restTemplate;

    public void incrementCommentCount(Long postId) {
        restTemplate.put(
            "http://POST-SERVICE/posts/" + postId + "/increment-comment",
            null
        );
    }

    public void decrementCommentCount(Long postId) {
        restTemplate.put(
            "http://POST-SERVICE/posts/" + postId + "/decrement-comment",
            null
        );
    }
}