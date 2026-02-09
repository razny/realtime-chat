package com.chat.app.controller;

import com.chat.app.config.RabbitConfig;
import com.chat.common.dto.DeleteMessageDTO;
import com.chat.common.dto.EditMessageDTO;
import com.chat.common.model.ChatMessage;
import com.chat.common.util.ColorDiagram;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ChatController {

    private final ColorDiagram colorDiagram = new ColorDiagram();
    private final Map<String, String> userColors = new ConcurrentHashMap<>();
    private final ApiController apiController;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private AmqpTemplate rabbitTemplate;

    public ChatController(ApiController apiController) {
        this.apiController = apiController;
    }

    @MessageMapping("/sendMessage")
    public void sendMessage(ChatMessage msg) {
        userColors.computeIfAbsent(msg.getSessionId(), k -> colorDiagram.getColor(msg.getSessionId()));
        msg.setColor(userColors.get(msg.getSessionId()));
        msg.setDateTime(new java.util.Date());

        String text = msg.getContent();
        if (text != null) {
            if (text.equalsIgnoreCase("/cat")) {
                // save the user's original command message
                rabbitTemplate.convertAndSend(RabbitConfig.CHAT_QUEUE, msg);

                // send the bot message to RabbitMQ
                ChatMessage botMsg = apiController.getCatImage();
                botMsg.setSessionId("bot-" + System.currentTimeMillis());
                botMsg.setColor("#FF8800");
                botMsg.setDateTime(new java.util.Date());
                rabbitTemplate.convertAndSend(RabbitConfig.CHAT_QUEUE, botMsg);
                return;
            } else if (text.equalsIgnoreCase("/dog")) {
                // save the user's original command message
                rabbitTemplate.convertAndSend(RabbitConfig.CHAT_QUEUE, msg);

                // send the bot message to RabbitMQ
                ChatMessage botMsg = apiController.getDogImage();
                botMsg.setSessionId("bot-" + System.currentTimeMillis());
                botMsg.setColor("#FF8800");
                botMsg.setDateTime(new java.util.Date());
                rabbitTemplate.convertAndSend(RabbitConfig.CHAT_QUEUE, botMsg);
                return;
            }
        }

        rabbitTemplate.convertAndSend(RabbitConfig.CHAT_QUEUE, msg);
    }

    @MessageMapping("/editMessage")
    public void editMessage(EditMessageDTO dto) {
        if (dto == null) {
            return;
        }

        String clientId = dto.getClientMessageId();
        boolean hasServerId = dto.getId() != null && !dto.getId().isBlank();
        boolean hasClientId = clientId != null && !clientId.isBlank();

        if (!hasServerId && !hasClientId) {
            return;
        }
        rabbitTemplate.convertAndSend(RabbitConfig.EDIT_QUEUE, dto);
    }

    @MessageMapping("/deleteMessage")
    public void deleteMessage(DeleteMessageDTO dto) {
        boolean hasServerId = dto.getId() != null && !dto.getId().isBlank();
        boolean hasClientId = dto.getClientMessageId() != null && !dto.getClientMessageId().isBlank();
        if (!hasServerId && !hasClientId) {
            return;
        }

        rabbitTemplate.convertAndSend(RabbitConfig.DELETE_QUEUE, dto);

        if (hasServerId) {
            messagingTemplate.convertAndSend("/topic/messageDeleted", Map.of("id", dto.getId()));
        } else {
            messagingTemplate.convertAndSend("/topic/messageDeleted", Map.of("clientMessageId", dto.getClientMessageId()));
        }
    }

    @GetMapping("chat")
    public String chat() {
        return "chat";
    }

    @GetMapping("/sessionColor/{sessionId}")
    @ResponseBody
    public String getSessionColor(@PathVariable String sessionId) {
        return colorDiagram.getColor(sessionId);
    }
}