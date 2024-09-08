package com.maharshi.bollywood_game_spring_boot.controller;

import com.maharshi.bollywood_game_spring_boot.dto.Response;
import com.maharshi.bollywood_game_spring_boot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class PrivateController {

    @Autowired
    private UserService userService;

    @GetMapping("/loadUsername")
    public ResponseEntity<Response> loadUsername()
    {
        return new ResponseEntity<>(new Response("success",userService.getCurrentUser().getUsername(),true),HttpStatus.OK);
    }
}
