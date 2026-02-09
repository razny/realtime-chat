package com.chat.app.repository;

import com.chat.common.document.ChatMessageDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessageDocument, String> {
    List<ChatMessageDocument> findByTimestampAfter(Date since);
}
