package com.chat.processor;

import com.chat.common.model.ChatMessage;
import com.chat.processor.repository.ChatMessageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChatMessageListenerIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ChatMessageRepository repository;

    @Test
    void testReceiveMessageSavesToDb() throws InterruptedException {
        ChatMessage msg = new ChatMessage("user", "integration test", "sess", "#8fc682ff");
        rabbitTemplate.convertAndSend("chatMessages", msg);

        Thread.sleep(500); // wait for listener

        List<com.chat.common.document.ChatMessageDocument> all = repository.findAll();
        assertTrue(all.stream().anyMatch(m -> "integration test".equals(m.getContent())));
    }
}