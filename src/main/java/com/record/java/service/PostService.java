package com.record.java.service;

import com.record.java.config.PostConfig;
import com.record.java.pojo.CommentPojo;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostConfig postConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public List<CommentPojo> fetchAllPost() {
        ResponseEntity<List<CommentPojo>> response = restTemplate.exchange(
                postConfig.getServiceUrl(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        return response.getBody();
    }
}
