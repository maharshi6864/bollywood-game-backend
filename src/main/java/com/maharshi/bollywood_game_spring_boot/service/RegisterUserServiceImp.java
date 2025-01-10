package com.maharshi.bollywood_game_spring_boot.service;


import com.maharshi.bollywood_game_spring_boot.dto.Response;
import com.maharshi.bollywood_game_spring_boot.model.OTPVo;
import com.maharshi.bollywood_game_spring_boot.model.PlayerVo;
import com.maharshi.bollywood_game_spring_boot.model.UserVo;
import com.maharshi.bollywood_game_spring_boot.repository.PlayerRepository;
import com.maharshi.bollywood_game_spring_boot.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class RegisterUserServiceImp implements RegisterUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private OTPService otpservice;

    @Autowired
    private PlayerService playerService;


    @Override
    public Response registerUser(UserVo userVo) {
        List<UserVo> ls = userRepository.findByUsername(userVo.getUsername());
        if (ls.isEmpty()) {
            userRepository.save(userVo);
            Map<?, ?> map = otpservice.sendOtpForRegistration(userVo);
            return new Response("user registered for verification....", map, true);
        }
        return new Response("username already exist can't register user !!", null, false);
    }

    @Override
    public Response checkUsernameAvailable(String username) {
        List<UserVo> ls = userRepository.findByUsername(username);
        if (ls.isEmpty()) {
            return new Response("Username available.", username, true);
        }
        return new Response("Username not available", username, false);
    }

    @Override
    public Response resendOtp(UserVo userVo) {
        List<UserVo> ls = userRepository.findByUsername(userVo.getUsername());
        if (ls.isEmpty()) {
            return new Response("username not found !!", null, false);
        }
        return new Response("otp resend for registration....", otpservice.resendOtpForRegistration(ls.get(0)), true);
    }

    @Override
    public Response validateOtp(Map<?, ?> requestObj) {
        OTPVo otpVo = new OTPVo();
        List<UserVo> ls = userRepository.findByUsername((String) requestObj.get("username"));
        if (ls.isEmpty()) {
            return new Response("username not found !!", null, false);
        }
        otpVo.setUser(ls.get(0));
        otpVo.setOtp((String) requestObj.get("otp"));
        Map<?, ?> map = otpservice.validateOtpForRegistration(otpVo);
        if (map.get("message").equals("valid")) {
            UserVo userVo = ls.get(0);
            userVo.setStatus(true);
            userRepository.save(userVo);
            PlayerVo playerVo =new PlayerVo();
            playerVo.setPlayerName(userVo.getUsername());
            PlayerVo playerVo1=this.playerService.savePLayer(playerVo);
            userVo.setPlayerVo(playerVo1);
        }
        return new Response("opt validation completed....", map, true);
    }

    @Override
    public Response confirmUserForRegistration(String username) {
        return null;
    }
}
