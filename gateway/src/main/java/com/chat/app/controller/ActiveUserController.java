package com.chat.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class ActiveUserController {
    private final Map<String, String> activeUsers = new ConcurrentHashMap<>();

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ActiveUserController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // track connection and broadcast updated list
    @org.springframework.context.event.EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = sha.getSessionId();
        String username = sha.getFirstNativeHeader("username");
        if (username != null && !username.isEmpty()) {
            activeUsers.put(sessionId, username);
            messagingTemplate.convertAndSend("/topic/activeUsers", new ArrayList<>(new LinkedHashSet<>(activeUsers.values())));
        }
    }

    // track disconnection and broadcast updated list
    @org.springframework.context.event.EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        activeUsers.remove(event.getSessionId());
        messagingTemplate.convertAndSend("/topic/activeUsers", new ArrayList<>(new LinkedHashSet<>(activeUsers.values())));
    }

    // endpoint to fetch active users
    @GetMapping("/activeUsers")
    public List<String> getActiveUsers() {
        return new ArrayList<>(new LinkedHashSet<>(activeUsers.values()));
    }
}