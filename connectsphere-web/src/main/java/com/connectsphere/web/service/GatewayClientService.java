package com.connectsphere.web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GatewayClientService {

    private final RestTemplate restTemplate;

    @Value("${connectsphere.gateway.base-url}")
    private String gatewayBaseUrl;

    public List<Map<String, Object>> getPublicPosts() {
        return exchangeList("/posts/public");
    }

    public List<Map<String, Object>> searchPosts(String keyword) {
        return exchangeList("/search?keyword=" + keyword);
    }

    public List<Map<String, Object>> searchUsers(String keyword) {
        return exchangeList("/api/v1/auth/search?keyword=" + keyword);
    }

    public Map<String, Object> getUserProfile(String username) {
        return getMap("/api/v1/auth/profile/" + username);
    }

    public List<Map<String, Object>> getUserPosts(Long userId) {
        return exchangeList("/posts/author/" + userId);
    }

    public List<Map<String, Object>> getNotifications(Long userId, String token) {
        return exchangeListWithAuth("/notifications/receiver/" + userId, token);
    }

    public List<Map<String, Object>> getReports(String token) {
        return exchangeListWithAuth("/reports", token);
    }

    public List<Map<String, Object>> getPayments(String token) {
        return exchangeListWithAuth("/payments", token);
    }

    public List<String> getTrendingHashtags() {
        return restTemplate.exchange(
                gatewayBaseUrl + "/search/hashtags/trending",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<List<String>>() {}
        ).getBody();
    }

    private Map<String, Object> getMap(String path) {
        Map<String, Object> body = restTemplate.getForObject(gatewayBaseUrl + path, Map.class);
        return body == null ? Collections.emptyMap() : body;
    }

    private List<Map<String, Object>> exchangeList(String path) {
        List<Map<String, Object>> body = restTemplate.exchange(
                gatewayBaseUrl + path,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        ).getBody();
        return body == null ? Collections.emptyList() : body;
    }

    private List<Map<String, Object>> exchangeListWithAuth(String path, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        List<Map<String, Object>> body = restTemplate.exchange(
                gatewayBaseUrl + path,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        ).getBody();
        return body == null ? Collections.emptyList() : body;
    }
}
