package com.maharshi.bollywood_game_spring_boot.service;


import com.maharshi.bollywood_game_spring_boot.dto.Response;
import com.maharshi.bollywood_game_spring_boot.model.OTPVo;
import com.maharshi.bollywood_game_spring_boot.model.User;
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


    @Override
    public Response registerUser(User user) {
        List<User> ls = userRepository.findByUsername(user.getUsername());
        if (ls.isEmpty()) {
            userRepository.save(user);
            Map<?, ?> map = otpservice.sendOtpForRegistration(user);
            return new Response("user registered for verification....", map, true);
        }
        return new Response("username already exist can't register user !!", null, false);
    }

    @Override
    public Response checkUsernameAvailable(String username) {
        List<User> ls = userRepository.findByUsername(username);
        if (ls.isEmpty()) {
            return new Response("Username available.", username, true);
        }
        return new Response("Username not available", username, false);
    }

    @Override
    public Response resendOtp(User user) {
        List<User> ls = userRepository.findByUsername(user.getUsername());
        if (ls.isEmpty()) {
            return new Response("username not found !!", null, false);
        }
        return new Response("otp resend for registration....", otpservice.resendOtpForRegistration(ls.get(0)), true);
    }

    @Override
    public Response validateOtp(Map<?, ?> requestObj) {
        OTPVo otpVo = new OTPVo();
        List<User> ls = userRepository.findByUsername((String) requestObj.get("username"));
        if (ls.isEmpty()) {
            return new Response("username not found !!", null, false);
        }
        otpVo.setUser(ls.get(0));
        otpVo.setOtp((String) requestObj.get("otp"));
        Map<?, ?> map = otpservice.validateOtpForRegistration(otpVo);
        if (map.get("message").equals("valid")) {
            User user = ls.get(0);
            user.setStatus(true);
            userRepository.save(user);
        }
        return new Response("opt validation completed....", map, true);
    }

    @Override
    public Response confirmUserForRegistration(String username) {
        return null;
    }
}
