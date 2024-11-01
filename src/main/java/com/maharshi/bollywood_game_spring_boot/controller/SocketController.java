package com.maharshi.bollywood_game_spring_boot.controller;

import com.maharshi.bollywood_game_spring_boot.dto.SocketRequest;
import com.maharshi.bollywood_game_spring_boot.dto.SocketResponse;
import com.maharshi.bollywood_game_spring_boot.model.UserVo;
import com.maharshi.bollywood_game_spring_boot.service.FriendsService;
import com.maharshi.bollywood_game_spring_boot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SocketController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private FriendsService friendsService;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @MessageMapping("/status")
    @SendTo("/topic/status")
    public SocketResponse updateStatus(SocketRequest socketRequest) {
        if (socketRequest.getMessage().equals("online"))
        {
            redisTemplate.opsForValue().set(socketRequest.getBody(),"online");
        }else{
            redisTemplate.delete(socketRequest.getBody());
        }
        SocketResponse response = new SocketResponse(socketRequest.getMessage(),socketRequest.getBody(), socketRequest.getType());
        return response;
    }

}