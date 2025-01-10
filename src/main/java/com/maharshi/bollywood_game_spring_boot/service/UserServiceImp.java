package com.maharshi.bollywood_game_spring_boot.service;


import com.maharshi.bollywood_game_spring_boot.dto.*;
import com.maharshi.bollywood_game_spring_boot.model.InGamePlayerVo;
import com.maharshi.bollywood_game_spring_boot.model.UserVo;
import com.maharshi.bollywood_game_spring_boot.repository.InGamePLayerRepository;
import com.maharshi.bollywood_game_spring_boot.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class UserServiceImp implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InGamePLayerRepository inGamePLayerRepository;

    @Autowired
    private RedisService redisService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public void insertUser(UserVo userVo) {
        userRepository.save(userVo);
    }

    @Override
    public UserVo findByUserName(String username) {
        return this.userRepository.findByUsername(username).get(0);
    }

    @Override
    public UserVo getCurrentUser() {
       try{
           UserVo userVo=new UserVo();
           userVo.setUsername(SecurityContextHolder.getContext().getAuthentication().getName());
           return userVo;
       } catch (Exception e) {
           System.out.println(e);
           return null;
       }
    }

    @Override
    public Response getUserDetails() {
        UserVo userVo=this.userRepository.findByUsername(SecurityContextHolder
                .getContext().getAuthentication().getName()).get(0);
        String inAGame=userVo.getPlayerVo().getGameVo()!=null?"INAGAME":"NOTINAGAME";
        UserDto userDto=new UserDto(userVo.getUsername(),userVo.getEmail(),userVo.getPlayerVo().getId(),
                userVo.getPlayerVo().getPlayerName(),userVo.getPlayerVo().getMatchesPlayed(),
                userVo.getPlayerVo().getPoints(),inAGame);
        List<InGamePlayerVo> inGamePlayerVoList =this.inGamePLayerRepository.findByJoinedStatus("Requested");

        if (!inGamePlayerVoList.isEmpty())
        {
            for (InGamePlayerVo inGamePlayerVo:inGamePlayerVoList)
            {
                GameDto gameDto=this.redisService.get(String.valueOf(inGamePlayerVo.getGameVo().getId())
                        ,GameDto.class);
                SocketResponse response = new
                        SocketResponse(userVo.getUsername(), gameDto, "gameRequest");
                messagingTemplate.convertAndSend("/topic/general", response);
            }
        }
        return new Response("sucess",userDto,true);
    }

}
