package com.maharshi.bollywood_game_spring_boot.controller;

import com.maharshi.bollywood_game_spring_boot.dto.Response;
import com.maharshi.bollywood_game_spring_boot.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("player")
public class PlayerController {

    @Autowired
    private PlayerService playerService;

    @GetMapping("/searchPlayer")
    public ResponseEntity<Response> searchPlayer(@RequestParam String searchString)
    {
        return new ResponseEntity<>(this.playerService.searchThroughString(searchString), HttpStatus.OK);
    }
}
