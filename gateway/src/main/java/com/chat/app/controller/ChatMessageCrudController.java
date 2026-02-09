package com.chat.app.controller;

import com.chat.common.model.ChatMessage;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;


import java.util.List;

// handles RESTful CRUD operations for chat messages by communicating with the message processor service
@RestController
@RequestMapping("/api/messages")
public class ChatMessageCrudController {
    private final RestTemplate restTemplate;
    private final String processorUrl;

    public ChatMessageCrudController(
        RestTemplate restTemplate,
        @Value("${processor.url}") String processorUrl
    ) {
        this.restTemplate = restTemplate;
        this.processorUrl = processorUrl;
    }

    @PostMapping
    public ChatMessage create(@RequestBody ChatMessage msg) {
        return restTemplate.postForObject(processorUrl, msg, ChatMessage.class);
    }

    @GetMapping
    public List<ChatMessage> getAll() {
        try {
            ResponseEntity<List<ChatMessage>> response = restTemplate.exchange(
                    processorUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<ChatMessage>>() {}
            );
            return response.getBody();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    @GetMapping("/{id}")
    public ChatMessage getOne(@PathVariable String id) {
        try {
            return restTemplate.getForObject(processorUrl + "/" + id, ChatMessage.class);
        } catch (RestClientException e) {
            return null;
        }
    }
    
    @PutMapping("/{id}")
    public ChatMessage update(@PathVariable String id, @RequestBody ChatMessage updated) {
        HttpEntity<ChatMessage> entity = new HttpEntity<>(updated);
        ResponseEntity<ChatMessage> response = restTemplate.exchange(
                processorUrl + "/" + id,
                HttpMethod.PUT,
                entity,
                ChatMessage.class
        );
        return response.getBody();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        restTemplate.delete(processorUrl + "/" + id);
    }
}
