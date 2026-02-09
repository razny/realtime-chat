package com.chat.app.controller;

import com.chat.app.repository.ChatMessageRepository;
import com.chat.common.document.ChatMessageDocument;
import com.chat.common.model.ChatMessage;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
public class ChatHistoryController {
    private final ChatMessageRepository repository;

    public ChatHistoryController(ChatMessageRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/recent")
    public List<ChatMessage> getRecent(@RequestParam("since") long sinceMillis) {
        Date since = new Date(sinceMillis);
        List<ChatMessageDocument> docs = repository.findByTimestampAfter(since);
        return docs.stream().map(doc -> {
            ChatMessage msg = new ChatMessage();
            msg.setId(doc.getId());
            msg.setSender(doc.getSender());
            msg.setContent(doc.getContent());
            msg.setSessionId(doc.getSessionId());
            msg.setColor(doc.getColor());
            msg.setDateTime(doc.getTimestamp()); // map timestamp -> dateTime
            msg.setClientMessageId(doc.getClientMessageId());
            return msg;
        }).collect(Collectors.toList());
    }
}