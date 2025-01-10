package com.maharshi.bollywood_game_spring_boot.service;


import com.maharshi.bollywood_game_spring_boot.CustomException.InvalidUserName;
import com.maharshi.bollywood_game_spring_boot.model.OTPVo;
import com.maharshi.bollywood_game_spring_boot.model.UserVo;
import com.maharshi.bollywood_game_spring_boot.repository.OTPRepository;
import com.maharshi.bollywood_game_spring_boot.utils.Utils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class OTPServiceImp implements OTPService {

    @Autowired
    private Utils utils;

    @Autowired
    private MailService mailService;

    @Autowired
    private OTPRepository otpRepository;

    @Override
    public Map<?, ?> sendOtpForRegistration(UserVo userVo) {
        String randomOtp = this.utils.generateOTP();
        OTPVo otpVo = new OTPVo();
        otpVo.setOtp(randomOtp);
        otpVo.setUser(userVo);

        // Calculate expiration time (e.g., 15 minutes from now)
        Instant expirationTime = Instant.now().plus(3, ChronoUnit.MINUTES);
        String expirationTimeISO = DateTimeFormatter.ISO_INSTANT.format(expirationTime);

        // HTML email body
        String emailBody = String.format("""
                        <html>
                               <head>
                                   <style>
                                       body {
                                           font-family: Arial, sans-serif;
                                           line-height: 1.6;
                                           color: #333;
                                           background-color: #f9f9f9;
                                           margin: 0;
                                           padding: 0;
                                       }
                
                                       .email-container {
                                           background: white;
                                           border: 1px solid #ddd;
                                           padding: 20px;
                                           max-width: 600px;
                                           margin: 20px auto;
                                           box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
                                           border-radius: 8px;
                                       }
                
                                       .header {
                                           background-color: #007BFF;
                                           color: white;
                                           padding: 10px 20px;
                                           text-align: center;
                                           font-size: 20px;
                                           border-radius: 8px 8px 0 0;
                                       }
                
                                       .footer {
                                           font-size: 12px;
                                           color: #777;
                                           text-align: center;
                                           margin-top: 20px;
                                       }
                
                                       .otp {
                                           font-size: 24px;
                                           font-weight: bold;
                                           color: #007BFF;
                                       }
                                   </style>
                               </head>
                
                               <body>
                                   <div class="email-container">
                                       <div class="header">Verify Your Registration</div>
                                       <p>Dear <strong>%s</strong>,</p>
                                       <p>Thank you for registering in <b>BOLLYWOOD</b> !</p>
                                       <p>Your OTP for registration is:</p>
                                       <p class="otp">%s</p>
                                       <p><small>(This OTP will expire in 3 minutes.)</small></p>
                                       <p>If you did not request this, please ignore this email.</p>
                                       <p>Thank you,<br></p>
                                       <div class="footer">
                                           <p>&copy; 2024 <b>BOLLYWOOD</b> . All rights reserved.</p>
                                       </div>
                                   </div>
                               </body>
                
                               </html>
                """, userVo.getUsername(), randomOtp);

        // Send the email
        mailService.sendMail(userVo.getEmail(), "VERIFY OTP FOR REGISTRATION", emailBody);

        otpVo.setExpireDate(expirationTime);
        otpRepository.save(otpVo);

        return Map.of("timeStamp", expirationTimeISO, "username", userVo.getUsername());
    }


    @Override
    public Map<?, ?> resendOtpForRegistration(UserVo userVo) {
        List<OTPVo> otpVos = this.otpRepository.findByUserVo(userVo);
        if (otpVos.isEmpty()) {
            throw new InvalidUserName("Invalid Username for Resend Otp");
        }
        otpRepository.delete(otpVos.get(0));
        String randomOtp = this.utils.generateOTP();
        OTPVo otpVo = new OTPVo();
        otpVo.setOtp(randomOtp);
        otpVo.setUser(userVo);
        // Calculate expiration time (e.g., 15 minutes from now)
        Instant expirationTime = Instant.now().plus(3, ChronoUnit.MINUTES);
        // Convert expiration time to ISO 8601 format
        String expirationTimeISO = DateTimeFormatter.ISO_INSTANT.format(expirationTime);
        String emailBody = String.format("Your New OTP for Username %s is\n%s \n(this otp will be valid for 3 min.)\nThank You For Registration.", userVo.getUsername(), randomOtp);
        mailService.sendMail(userVo.getEmail(), "VERIFY OTP FOR REGISTRATION", emailBody);
        otpVo.setExpireDate(expirationTime);
        otpRepository.save(otpVo);
        return Map.of("timeStamp", expirationTimeISO, "username", userVo.getUsername());
    }

    @Override
    public String getOtpExpriration(String username) {
        return "";
    }

    @Override
    public Map<?, ?> validateOtpForRegistration(OTPVo otpVo) {

        List<OTPVo> otpVos = otpRepository.findByUserVo(otpVo.getUser());

        if (otpVos.isEmpty()) {
            return Map.of("message", "Otp Not found");
        }

        OTPVo otpVoStored = otpVos.get(0);

        Instant now = Instant.now();
        if (now.isAfter(otpVoStored.getExpireDate())) {
            return Map.of("message", "otp expired");
        }

        if (!otpVo.getOtp().equals(otpVoStored.getOtp())) {
            return Map.of("message", "invalid otp");
        }

        otpRepository.delete(otpVoStored);
        return Map.of("message", "valid");
    }
}
