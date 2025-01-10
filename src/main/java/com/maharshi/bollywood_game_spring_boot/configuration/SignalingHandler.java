package com.maharshi.bollywood_game_spring_boot.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SignalingHandler extends TextWebSocketHandler {

    private final Map<String, List<WebSocketSession>> rooms = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Remove the session from all rooms
        for (Map.Entry<String, List<WebSocketSession>> entry : rooms.entrySet()) {
            entry.getValue().remove(session);
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, Object> value = parseMessage(message);

        String type = (String) value.get("type");
        String roomId = (String) value.get("roomId");
        String receiverId = (String) value.get("receiverId");

        switch (type) {
            case "join":
                rooms.computeIfAbsent(roomId, k -> new ArrayList<>()).add(session);
                break;

            case "offer":
            case "answer":
            case "ice-candidate":
                List<WebSocketSession> clients = rooms.get(roomId);
                if (clients != null) {
                    for (WebSocketSession client : clients) {
                        if (!client.equals(session)) {
                            client.sendMessage(new TextMessage(message.getPayload()));
                        }
                    }
                }
                break;
        }
    }

    private Map<String, Object> parseMessage(TextMessage message) throws Exception {
        // Convert JSON message to Map (requires Jackson library)
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(message.getPayload(), Map.class);
    }
}
