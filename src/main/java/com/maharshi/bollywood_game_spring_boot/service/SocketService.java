package com.maharshi.bollywood_game_spring_boot.service;

import com.maharshi.bollywood_game_spring_boot.dto.SocketResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Service
public class SocketService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private PlayerService playerService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        String username = event.getUser().getName();
        playerService.updatePlayerStatus(username,"Online",null);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String username = event.getUser().getName();
        playerService.updatePlayerStatus(username,"Offline",null);
    }
}
