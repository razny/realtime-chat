package com.chat.processor.service;

import com.chat.common.document.ChatMessageDocument;
import com.chat.common.model.ChatMessage;
import com.chat.processor.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatMessageService {

    private final ChatMessageRepository repository;

    public ChatMessageService(ChatMessageRepository repository) {
        this.repository = repository;
    }

    private ChatMessageDocument toDocument(ChatMessage msg) {
        ChatMessageDocument doc = new ChatMessageDocument();
        doc.setSender(msg.getSender());
        doc.setContent(msg.getContent());
        doc.setTimestamp(msg.getDateTime());
        doc.setSessionId(msg.getSessionId());
        doc.setColor(msg.getColor());
        doc.setClientMessageId(msg.getClientMessageId());
        return doc;
    }

    private ChatMessage toModel(ChatMessageDocument doc) {
        ChatMessage msg = new ChatMessage();
        msg.setId(doc.getId());
        msg.setSender(doc.getSender());
        msg.setContent(doc.getContent());
        msg.setDateTime(doc.getTimestamp());
        msg.setSessionId(doc.getSessionId());
        msg.setColor(doc.getColor());
        msg.setClientMessageId(doc.getClientMessageId());
        return msg;
    }

    public ChatMessage save(ChatMessage msg) {
        ChatMessageDocument doc = toDocument(msg);
        ChatMessageDocument saved = repository.save(doc);
        return toModel(saved);
    }

    public List<ChatMessage> getAll() {
        return repository.findAll().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public ChatMessage getOne(String id) {
        return repository.findById(id)
                .map(this::toModel)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono wiadomości"));
    }

    public ChatMessage updateContent(String id, String content) {
        ChatMessageDocument doc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono wiadomości"));
        doc.setContent(content);
        ChatMessageDocument saved = repository.save(doc);
        return toModel(saved);
    }

    // update based on clientMessageId (if server id is not known)
    public ChatMessage updateContentByClientMessageId(String clientMessageId, String content) {
        var opt = repository.findByClientMessageId(clientMessageId);
        if (opt.isEmpty()) {
            throw new RuntimeException("Nie znaleziono wiadomości (clientMessageId=" + clientMessageId + ")");
        }
        ChatMessageDocument doc = opt.get();
        doc.setContent(content);
        ChatMessageDocument saved = repository.save(doc);
        return toModel(saved);
    }

    public List<ChatMessage> getSince(Date since) {
        return repository.findByTimestampAfter(since).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public void delete(String id) {
        if (!repository.existsById(id)) throw new RuntimeException("Nie znaleziono wiadomości");
        repository.deleteById(id);
    }

    public boolean deleteByClientMessageId(String clientMessageId) {
        if (clientMessageId == null || clientMessageId.isBlank()) {
            return false;
        }

        try {
            var opt = repository.findByClientMessageId(clientMessageId);
            if (opt.isPresent()) {
                String id = opt.get().getId();
                repository.deleteById(id);
                return true;
            }

            try {
                repository.deleteByClientMessageId(clientMessageId);
                return true;
            } catch (Exception e) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}