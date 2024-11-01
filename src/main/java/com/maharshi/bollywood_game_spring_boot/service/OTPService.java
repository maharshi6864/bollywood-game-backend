package com.maharshi.bollywood_game_spring_boot.service;




import com.maharshi.bollywood_game_spring_boot.model.OTPVo;
import com.maharshi.bollywood_game_spring_boot.model.UserVo;

import java.util.Map;

public interface OTPService {

    Map<?, ?> sendOtpForRegistration(UserVo userVo);

    Map<?, ?> resendOtpForRegistration(UserVo userVo);

    String getOtpExpriration(String username);

    Map<?, ?> validateOtpForRegistration(OTPVo otpVo);
}
