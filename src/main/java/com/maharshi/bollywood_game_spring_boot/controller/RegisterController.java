package com.maharshi.bollywood_game_spring_boot.controller;

import com.maharshi.bollywood_game_spring_boot.dto.Response;
import com.maharshi.bollywood_game_spring_boot.model.User;
import com.maharshi.bollywood_game_spring_boot.service.RegisterUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/register")
@CrossOrigin(origins = "http://localhost:5173")
public class RegisterController {

    @Autowired
    private RegisterUserService registerUserService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("")
    public ResponseEntity<Response> register(@RequestBody User user) {
        user.setRole("USER");
        user.setStatus(false);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return new ResponseEntity<Response>(registerUserService.registerUser(user), HttpStatus.OK);
    }

    @PostMapping("/checkUsernameAvail")
    public ResponseEntity<Response> checkUsernameAvail(@RequestBody String username) {
        return new ResponseEntity<Response>(registerUserService.checkUsernameAvailable(username), HttpStatus.OK);
    }

    @PostMapping("/confirmUserForRegistration")
    public ResponseEntity<Response> confirmUserForRegistration(@RequestBody String username) {
        return new ResponseEntity<Response>(registerUserService.confirmUserForRegistration(username), HttpStatus.OK);
    }

    @PostMapping("/resendOtp")
    public ResponseEntity<Response> resendOtp(@RequestBody User user) {
        return new ResponseEntity<Response>(registerUserService.resendOtp(user), HttpStatus.OK);
    }

    @PostMapping("/validateOtp")
    public ResponseEntity<Response> validateOtp(@RequestBody Map<?, ?> requestObj) {
        return new ResponseEntity<Response>(registerUserService.validateOtp(requestObj), HttpStatus.OK);
    }
}