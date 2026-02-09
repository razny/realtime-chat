package com.chat.app.listener;

import com.chat.app.config.RabbitConfig;
import com.chat.common.model.ChatMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class SavedMessageListener {

    private final SimpMessagingTemplate messagingTemplate;

    public SavedMessageListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(queues = RabbitConfig.SAVED_QUEUE)
    public void handleSavedMessage(ChatMessage saved) {
        // forward object to websocket subscribers
        messagingTemplate.convertAndSend("/topic/messages", saved);
    }
}