package com.chat.app.controller;

import com.chat.common.model.ChatMessage;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
public class ApiController {
    private final RestTemplate restTemplate;

    public ApiController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private String getTimestamp() {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
    }

    @GetMapping("/api/cat")
    public ChatMessage getCatImage() {
        String url = "https://api.thecatapi.com/v1/images/search";
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

        List<Map<String, Object>> cat = response.getBody();
        String imageUrl = (cat != null && !cat.isEmpty()) ? (String) cat.get(0).get("url") : "no-image";
        return new ChatMessage("CatBot", imageUrl, "bot", "#FF69B4");
    }

    @GetMapping("/api/dog")
    public ChatMessage getDogImage() {
        String url = "https://dog.ceo/api/breeds/image/random";
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        Map<String, Object> dog = response.getBody();
        String imageUrl = (dog != null) ? (String) dog.get("message") : "no-image";
        return new ChatMessage("DogBot", imageUrl, "bot", "#1E90FF");
    }
}