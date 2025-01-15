package com.maharshi.bollywood_game_spring_boot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maharshi.bollywood_game_spring_boot.dto.*;
import com.maharshi.bollywood_game_spring_boot.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.MessagingAdviceBean;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/game")
public class GameController {

    @Autowired
    private GameService gameService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private RedisService redisService;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlayerService playerService;

    @PostMapping("/startGame")
    public ResponseEntity<Response> startGame(@RequestBody GameDto gameDto) {
        Response response = this.gameService.startGame(gameDto);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/gameRequestReply")
    public ResponseEntity<Response> gameRequestDeclined(@RequestBody PlayerStatusDto playerStatusDto) {
        Response response = this.gameService.gameRequestReply(playerStatusDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getGameDetails")
    public ResponseEntity<Response> getGameDetails() {
        Response response = this.gameService.getGameDetails();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @MessageMapping("/game")
    public void game(SocketRequest socketRequest) {
        try {
            log.info(socketRequest.toString());
            switch (socketRequest.getMessage()) {
                case "AskingGuessingStatus" -> this.gameService.askingGuessingStatus(socketRequest);
                case "playerInGameStatus" ->
                        this.gameService.changePlayerInGameStatus((PlayerStatusDto) socketRequest.getBody());
                case "movieNameAsked" -> this.gameService.movieNameAsked(objectMapper
                        .convertValue(socketRequest.getBody(), AskingAndGuessingDto.class));
                case "guessUpdated" -> this.gameService.guessMovieName(objectMapper
                        .convertValue(socketRequest.getBody(), AskingAndGuessingDto.class));
                case "CompleteThisRound" -> this.gameService.completeThisRound(socketRequest);
                case "hintPassed"->this.gameService.passHint(socketRequest);
            }
        } catch (Exception e) {
            log.info(e.toString());
        }
    }


    @PostMapping("/endGame")
    public ResponseEntity<Response> endGame(@RequestBody GameDto gameDto) {
        Response response = this.gameService.endGame(gameDto);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/reSendRequestToPlayer")
    public ResponseEntity<Response> reSendRequest(@RequestBody PlayerStatusDto playerStatusDto) {
        Response response = this.gameService.reSendRequest(playerStatusDto);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/getHintFromAi")
    public ResponseEntity<Response> getHintFromAi(@RequestBody GameDto gameDto) {
        Response response= this.gameService.getHintForMovieFromChatGpt(gameDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}