package com.chat.processor.repository;

import com.chat.common.document.ChatMessageDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends MongoRepository<ChatMessageDocument, String> {
    Optional<ChatMessageDocument> findByClientMessageId(String clientMessageId);
    void deleteByClientMessageId(String clientMessageId);

    List<ChatMessageDocument> findByTimestampAfter(Date since);
}