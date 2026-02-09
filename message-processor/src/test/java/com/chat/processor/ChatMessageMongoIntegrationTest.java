package com.chat.processor;

import com.chat.common.model.ChatMessage;
import com.chat.processor.repository.ChatMessageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChatMessageMongoIntegrationTest {

    @Autowired
    private ChatMessageRepository repository;

    @Test
    void testSaveAndFind() {
        String testClientId = "test-client-id-123";
        repository.deleteAll(); 
        
        ChatMessage msg = new ChatMessage("mongoUser", "mongoTest", "sess", "#abc");
        com.chat.common.document.ChatMessageDocument doc = new com.chat.common.document.ChatMessageDocument();
        doc.setSender(msg.getSender());
        doc.setContent(msg.getContent());
        doc.setSessionId(msg.getSessionId());
        doc.setColor(msg.getColor());
        doc.setTimestamp(msg.getDateTime());
        doc.setClientMessageId(testClientId);

        repository.save(doc);

        assertTrue(repository.findByClientMessageId(testClientId).isPresent());
    }
}