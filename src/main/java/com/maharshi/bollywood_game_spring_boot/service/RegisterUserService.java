package com.maharshi.bollywood_game_spring_boot.service;




import com.maharshi.bollywood_game_spring_boot.dto.Response;
import com.maharshi.bollywood_game_spring_boot.model.UserVo;

import java.util.Map;

public interface RegisterUserService {

    Response registerUser(UserVo userVo);

    Response checkUsernameAvailable(String username);

    Response confirmUserForRegistration(String username);

    Response resendOtp(UserVo userVo);

    Response validateOtp(Map<?,?> requestObj);
}
