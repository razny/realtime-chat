package com.chat.processor.listener;

import com.chat.common.dto.DeleteMessageDTO;
import com.chat.common.dto.EditMessageDTO;
import com.chat.common.model.ChatMessage;
import com.chat.processor.config.RabbitConfig;
import com.chat.processor.service.ChatMessageService;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageListener {
    private final ChatMessageService service;
    private final AmqpTemplate rabbitTemplate;

    public ChatMessageListener(ChatMessageService service, AmqpTemplate rabbitTemplate) {
        this.service = service;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitConfig.CHAT_QUEUE)
    public void receiveMessage(ChatMessage msg) {
        ChatMessage saved = service.save(msg);
        rabbitTemplate.convertAndSend(RabbitConfig.SAVED_QUEUE, saved);
    }

    @RabbitListener(queues = RabbitConfig.EDIT_QUEUE)
    public void receiveEdit(EditMessageDTO dto) {
        if (dto == null) {
            return;
        }

        try {
            ChatMessage updated;
            if (dto.getId() != null && !dto.getId().isBlank()) {
                updated = service.updateContent(dto.getId(), dto.getContent());
            } else if (dto.getClientMessageId() != null && !dto.getClientMessageId().isBlank()) {
                updated = service.updateContentByClientMessageId(dto.getClientMessageId(), dto.getContent());
            } else {
                return;
            }
            rabbitTemplate.convertAndSend(RabbitConfig.EDITED_QUEUE, updated);
        } catch (Exception ignored) {
            //
        }
    }

    @RabbitListener(queues = RabbitConfig.DELETE_QUEUE)
    public void receiveDelete(DeleteMessageDTO dto) {
        if (dto == null) {
            return;
        }

        try {
            if (dto.getId() != null && !dto.getId().isBlank()) {
                service.delete(dto.getId());
            } else if (dto.getClientMessageId() != null && !dto.getClientMessageId().isBlank()) {
                service.deleteByClientMessageId(dto.getClientMessageId());
            } else {
                return;
            }

            rabbitTemplate.convertAndSend(RabbitConfig.DELETED_QUEUE, dto);

        } catch (Exception ignored) {
            //
        }
    }
}