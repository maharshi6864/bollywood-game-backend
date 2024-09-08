package com.maharshi.bollywood_game_spring_boot.service;




import com.maharshi.bollywood_game_spring_boot.dto.Response;
import com.maharshi.bollywood_game_spring_boot.model.User;

import java.util.Map;

public interface RegisterUserService {

    Response registerUser(User user);

    Response checkUsernameAvailable(String username);

    Response confirmUserForRegistration(String username);

    Response resendOtp(User user);

    Response validateOtp(Map<?,?> requestObj);
}
