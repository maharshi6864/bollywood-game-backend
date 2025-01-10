package com.maharshi.bollywood_game_spring_boot.controller;

import com.maharshi.bollywood_game_spring_boot.dto.Response;
import com.maharshi.bollywood_game_spring_boot.service.PlayerService;
import com.maharshi.bollywood_game_spring_boot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PlayerService playerService;

    @GetMapping("user/getUserDetails")
    public ResponseEntity<Response> loadUsername() {
//        playerService.updatePlayerStatus(userService.getCurrentUser().getUsername(),"Online",null);
        return new ResponseEntity<>(userService.getUserDetails(),
                HttpStatus.OK);
    }
}
