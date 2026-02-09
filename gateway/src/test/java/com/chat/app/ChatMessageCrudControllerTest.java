package com.chat.app;

import com.chat.common.model.ChatMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.chat.app.controller.ChatMessageCrudController;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatMessageCrudControllerTest {

    @Test
    void testCreate() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        String processorUrl = "http://localhost:8081/api/messages";
        ChatMessageCrudController controller = new ChatMessageCrudController(restTemplate, processorUrl);

        ChatMessage msg = new ChatMessage("user", "hi", "sess", "#fff");
        ChatMessage returned = new ChatMessage("user", "hi", "sess", "#fff");
        when(restTemplate.postForObject(anyString(), eq(msg), eq(ChatMessage.class))).thenReturn(returned);

        ChatMessage result = controller.create(msg);
        assertEquals("hi", result.getContent());
    }

    @Test
    void testGetAll() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        String processorUrl = "http://localhost:8081/api/messages";
        ChatMessageCrudController controller = new ChatMessageCrudController(restTemplate, processorUrl);

        List<ChatMessage> messages = List.of(new ChatMessage("user", "msg", "sess", "#fff"));
        ResponseEntity<List<ChatMessage>> response = new ResponseEntity<>(messages, HttpStatus.OK);

        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            isNull(),
            ArgumentMatchers.<ParameterizedTypeReference<List<ChatMessage>>>any()
        )).thenReturn(response);

        List<ChatMessage> result = controller.getAll();
        assertEquals(1, result.size());
    }

    @Test
    void testGetOneReturnsNullOnException() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        String processorUrl = "http://localhost:8081/api/messages";
        ChatMessageCrudController controller = new ChatMessageCrudController(restTemplate, processorUrl);

        when(restTemplate.getForObject(anyString(), eq(ChatMessage.class)))
            .thenThrow(new RestClientException("fail"));
        ChatMessage result = controller.getOne("id");
        assertNull(result);
    }

    @Test
    void testUpdateReturnsBody() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        String processorUrl = "http://localhost:8081/api/messages";
        ChatMessageCrudController controller = new ChatMessageCrudController(restTemplate, processorUrl);

        ChatMessage updated = new ChatMessage("user", "updated", "sess", "#fff");
        ResponseEntity<ChatMessage> response = new ResponseEntity<>(updated, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(ChatMessage.class))).thenReturn(response);

        ChatMessage result = controller.update("id", updated);
        assertEquals("updated", result.getContent());
    }

    @Test
    void testDeleteDoesNotThrow() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        String processorUrl = "http://localhost:8081/api/messages";
        ChatMessageCrudController controller = new ChatMessageCrudController(restTemplate, processorUrl);

        doNothing().when(restTemplate).delete(anyString());
        assertDoesNotThrow(() -> controller.delete("id"));
    }
}