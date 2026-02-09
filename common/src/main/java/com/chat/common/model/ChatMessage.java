package com.chat.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessage {
    private String id; // string for mongoDB
    private String sender;
    private String content;
    private String sessionId;
    private String color;
    private Date dateTime;

    // client side temporary message ID
    @JsonProperty("clientMessageId")
    private String clientMessageId;

    public ChatMessage(String sender, String content, String sessionId, String color) {
        this.sender = sender;
        this.content = content;
        this.sessionId = sessionId;
        this.color = color;
        this.dateTime = new Date();
    }

    public ChatMessage(String sender, String content, String sessionId, String color, String clientMessageId) {
        this(sender, content, sessionId, color);
        this.clientMessageId = clientMessageId;
    }
}
