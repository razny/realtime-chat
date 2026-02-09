package com.chat.processor;

import com.chat.common.document.ChatMessageDocument;
import com.chat.common.model.ChatMessage;
import com.chat.processor.repository.ChatMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.chat.processor.service.ChatMessageService;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatMessageServiceTest {

    private ChatMessageRepository repository;
    private ChatMessageService service;

    @BeforeEach
    void setUp() {
        repository = mock(ChatMessageRepository.class);
        service = new ChatMessageService(repository);
    }

    @Test
    void testSaveAndGetOne() {
        ChatMessage msg = new ChatMessage("user", "hello", "sess", "#fff");
        ChatMessageDocument doc = new ChatMessageDocument();
        doc.setId("id1");
        doc.setSender("user");
        doc.setContent("hello");
        doc.setSessionId("sess");
        doc.setColor("#fff");
        doc.setTimestamp(new Date());

        when(repository.save(any())).thenReturn(doc);
        when(repository.findById("id1")).thenReturn(Optional.of(doc));

        ChatMessage saved = service.save(msg);
        assertEquals("user", saved.getSender());

        ChatMessage fetched = service.getOne("id1");
        assertEquals("hello", fetched.getContent());
    }

    @Test
    void testUpdateContent() {
        ChatMessageDocument doc = new ChatMessageDocument();
        doc.setId("id2");
        doc.setContent("old");
        when(repository.findById("id2")).thenReturn(Optional.of(doc));
        when(repository.save(any())).thenReturn(doc);

        ChatMessage updated = service.updateContent("id2", "new");
        assertEquals("new", updated.getContent());
    }

    @Test
    void testDelete() {
        when(repository.existsById("id3")).thenReturn(true);
        doNothing().when(repository).deleteById("id3");
        assertDoesNotThrow(() -> service.delete("id3"));
    }

    @Test
    void testGetAllReturnsEmptyList() {
        when(repository.findAll()).thenReturn(List.of());
        List<ChatMessage> result = service.getAll();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetOneThrowsIfNotFound() {
        when(repository.findById("missing")).thenReturn(Optional.empty());
        Exception ex = assertThrows(RuntimeException.class, () -> service.getOne("missing"));
        assertTrue(ex.getMessage().contains("Nie znaleziono wiadomości"));
    }

    @Test
    void testUpdateContentByClientMessageIdThrowsIfNotFound() {
        when(repository.findByClientMessageId("missing")).thenReturn(Optional.empty());
        Exception ex = assertThrows(RuntimeException.class, () -> service.updateContentByClientMessageId("missing", "content"));
        assertTrue(ex.getMessage().contains("Nie znaleziono wiadomości"));
    }

    @Test
    void testGetSinceReturnsMessages() {
        ChatMessageDocument doc = new ChatMessageDocument();
        doc.setId("id4");
        doc.setSender("user");
        doc.setContent("since");
        doc.setTimestamp(new Date());
        when(repository.findByTimestampAfter(any())).thenReturn(List.of(doc));
        List<ChatMessage> result = service.getSince(new Date());
        assertEquals(1, result.size());
        assertEquals("since", result.get(0).getContent());
    }

    @Test
    void testDeleteThrowsIfNotFound() {
        when(repository.existsById("missing")).thenReturn(false);
        Exception ex = assertThrows(RuntimeException.class, () -> service.delete("missing"));
        assertTrue(ex.getMessage().contains("Nie znaleziono wiadomości"));
    }

    @Test
    void testDeleteByClientMessageIdReturnsFalseIfNullOrBlank() {
        assertFalse(service.deleteByClientMessageId(null));
        assertFalse(service.deleteByClientMessageId(""));
        assertFalse(service.deleteByClientMessageId("   "));
    }

    @Test
    void testDeleteByClientMessageIdDeletesById() {
        ChatMessageDocument doc = new ChatMessageDocument();
        doc.setId("id5");
        when(repository.findByClientMessageId("clientId")).thenReturn(Optional.of(doc));
        doNothing().when(repository).deleteById("id5");
        boolean result = service.deleteByClientMessageId("clientId");
        assertTrue(result);
    }

    @Test
    void testDeleteByClientMessageIdDeletesByClientMessageId() {
        when(repository.findByClientMessageId("clientId2")).thenReturn(Optional.empty());
        doNothing().when(repository).deleteByClientMessageId("clientId2");
        boolean result = service.deleteByClientMessageId("clientId2");
        assertTrue(result);
    }
}