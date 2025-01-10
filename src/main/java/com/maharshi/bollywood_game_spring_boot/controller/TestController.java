package com.maharshi.bollywood_game_spring_boot.controller;

import com.maharshi.bollywood_game_spring_boot.dto.*;
import com.maharshi.bollywood_game_spring_boot.model.PlayerVo;
import com.maharshi.bollywood_game_spring_boot.model.UserVo;
import com.maharshi.bollywood_game_spring_boot.repository.GameRepository;
import com.maharshi.bollywood_game_spring_boot.repository.InGamePLayerRepository;
import com.maharshi.bollywood_game_spring_boot.service.GameService;
import com.maharshi.bollywood_game_spring_boot.service.GeminiAiService;
import com.maharshi.bollywood_game_spring_boot.service.OTPServiceImp;
import com.maharshi.bollywood_game_spring_boot.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class TestController {

    @Autowired
    private RedisService redisService;

    @Autowired
    private GameService gameService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private InGamePLayerRepository inGamePLayerRepository;

    @Autowired
    private OTPServiceImp otpServiceImp;

    @Autowired
    private GeminiAiService geminiAiService;

    @GetMapping("/test")
    public ResponseEntity<Response> test() {

        UserVo userVo=new UserVo(52,"username","devp0devp024@gmail.com","121212","USER",true,null);
        this.otpServiceImp.sendOtpForRegistration(userVo);
        return new ResponseEntity<>(null, HttpStatusCode.valueOf(200));
    }

    @GetMapping("/emptyDatabase")
    public ResponseEntity<Response> emptyDatabase() {
        this.inGamePLayerRepository.deleteAll();
        this.gameRepository.deleteAll();
        Response response=new Response("Success","okay",true);
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(200));
    }

    @GetMapping("/test1234")
    public ResponseEntity<Response> test1234() {
        String movieName="Race 2";
        String gemResponse=this.geminiAiService
                .askChatGpt("My friend is guessing this movie \""+movieName+"\" " +
                "from bollywood give either actress name or " +
                "actors name or song name from this movie, only one hint for this movie in description !");
        Response response=new Response("Success",gemResponse,true);
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(200));
    }
}
