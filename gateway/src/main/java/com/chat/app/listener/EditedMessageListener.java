package com.chat.app.listener;

import com.chat.app.config.RabbitConfig;
import com.chat.common.dto.DeleteMessageDTO;
import com.chat.common.model.ChatMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EditedMessageListener {

    private final SimpMessagingTemplate messagingTemplate;

    public EditedMessageListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(queues = RabbitConfig.EDITED_QUEUE)
    public void handleEditedMessage(ChatMessage updatedMessage) {
        messagingTemplate.convertAndSend("/topic/messageEdited", updatedMessage);
    }

    @RabbitListener(queues = RabbitConfig.DELETED_QUEUE)
    public void handleDeletedMessage(DeleteMessageDTO dto) {
        // forward either server id or clientMessageId so clients can remove the correct element
        if (dto.getId() != null && !dto.getId().isBlank()) {
            messagingTemplate.convertAndSend("/topic/messageDeleted", Map.of("id", dto.getId()));
        } else if (dto.getClientMessageId() != null && !dto.getClientMessageId().isBlank()) {
            messagingTemplate.convertAndSend("/topic/messageDeleted", Map.of("clientMessageId", dto.getClientMessageId()));
        }
    }
}