package com.maharshi.bollywood_game_spring_boot.controller;

import com.maharshi.bollywood_game_spring_boot.dto.PlayerDto;
import com.maharshi.bollywood_game_spring_boot.dto.Response;
import com.maharshi.bollywood_game_spring_boot.service.FriendsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("friend")
public class FriendsController {

    @Autowired
    private FriendsService friendsService;

    @GetMapping("/getFriends")
    public ResponseEntity<Response> getFriends() {
        Response response = this.friendsService.findFriendsForPlayer();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/saveFriend")
    public ResponseEntity<Response> saveFriend(@RequestBody PlayerDto playerDto) {
        Response response = this.friendsService.saveFriend(playerDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/removeFriend")
    public ResponseEntity<Response> removeFriend(@RequestParam int id) {
        Response response = this.friendsService.removefriend(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
