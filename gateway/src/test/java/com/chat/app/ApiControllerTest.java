package com.chat.app;

import com.chat.app.controller.ApiController;
import com.chat.common.model.ChatMessage;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.mockito.ArgumentMatchers;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApiControllerTest {

    @Test
    void testGetCatImage() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        ApiController controller = new ApiController(restTemplate);

        List<Map<String, Object>> catApiResponse = List.of(Map.of("url", "http://cat.jpg"));
        ResponseEntity<List<Map<String, Object>>> response = new ResponseEntity<>(catApiResponse, HttpStatus.OK);

        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            isNull(),
            ArgumentMatchers.<ParameterizedTypeReference<List<Map<String, Object>>>>any()
        )).thenReturn(response);

        ChatMessage result = controller.getCatImage();
        assertEquals("http://cat.jpg", result.getContent());
        assertEquals("CatBot", result.getSender());
    }

    @Test
    void testGetDogImage() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        ApiController controller = new ApiController(restTemplate);

        Map<String, Object> dogApiResponse = Map.of("message", "http://dog.jpg");
        ResponseEntity<Map<String, Object>> response = new ResponseEntity<>(dogApiResponse, HttpStatus.OK);

        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            isNull(),
            ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()
        )).thenReturn(response);

        ChatMessage result = controller.getDogImage();
        assertEquals("http://dog.jpg", result.getContent());
        assertEquals("DogBot", result.getSender());
    }
}