package com.maharshi.bollywood_game_spring_boot.service;




import com.maharshi.bollywood_game_spring_boot.model.OTPVo;
import com.maharshi.bollywood_game_spring_boot.model.User;

import java.util.Map;

public interface OTPService {

    Map<?, ?> sendOtpForRegistration(User user);

    Map<?, ?> resendOtpForRegistration(User user);

    String getOtpExpriration(String username);

    Map<?, ?> validateOtpForRegistration(OTPVo otpVo);
}
