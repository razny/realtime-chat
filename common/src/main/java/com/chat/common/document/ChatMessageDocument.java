package com.chat.common.document;

    import lombok.Data;
    import java.util.Date;
    import org.springframework.data.annotation.Id;
    import org.springframework.data.mongodb.core.mapping.Document;

    @Data
    @Document(collection = "chat_messages")
    public class ChatMessageDocument {

        @Id
        private String id;
        private String sender;
        private String content;
        private String sessionId;
        private String color;
        private Date timestamp;

        // keep client side id to map back to client messages (for edit/delete)
        private String clientMessageId;
    }