package com.maharshi.bollywood_game_spring_boot.service;


import com.maharshi.bollywood_game_spring_boot.dto.Response;
import com.maharshi.bollywood_game_spring_boot.dto.UserDto;
import com.maharshi.bollywood_game_spring_boot.model.UserVo;
import com.maharshi.bollywood_game_spring_boot.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class UserServiceImp implements UserService {

    @Autowired
    private UserRepository userRepository;

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
        return new Response("sucess",userDto,true);
    }

}
