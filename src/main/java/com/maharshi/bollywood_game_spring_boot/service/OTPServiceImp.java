package com.maharshi.bollywood_game_spring_boot.service;


import com.maharshi.bollywood_game_spring_boot.CustomException.InvalidUserName;
import com.maharshi.bollywood_game_spring_boot.model.OTPVo;
import com.maharshi.bollywood_game_spring_boot.model.User;
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
    public Map<?, ?> sendOtpForRegistration(User user) {
        String randomOtp = this.utils.generateOTP();
        OTPVo otpVo = new OTPVo();
        otpVo.setOtp(randomOtp);
        otpVo.setUser(user);
        // Calculate expiration time (e.g., 15 minutes from now)
        Instant expirationTime = Instant.now().plus(3, ChronoUnit.MINUTES);
        // Convert expiration time to ISO 8601 format
        String expirationTimeISO = DateTimeFormatter.ISO_INSTANT.format(expirationTime);
        String emailBody = String.format("Your OTP for Username %s is\n%s \n(this otp will be valid for 3 min.)\nThank You For Registration.", user.getUsername(), randomOtp);
        mailService.sendMail(user.getEmail(), "VERIFY OTP FOR REGISTRATION", emailBody);
        otpVo.setExpireDate(expirationTime);
        otpRepository.save(otpVo);
        return Map.of("timeStamp", expirationTimeISO, "username", user.getUsername());
    }

    @Override
    public Map<?, ?> resendOtpForRegistration(User user) {
        List<OTPVo> otpVos = this.otpRepository.findByUser(user);
        if (otpVos.isEmpty()) {
            throw new InvalidUserName("Invalid Username for Resend Otp");
        }
        otpRepository.delete(otpVos.get(0));
        String randomOtp = this.utils.generateOTP();
        OTPVo otpVo = new OTPVo();
        otpVo.setOtp(randomOtp);
        otpVo.setUser(user);
        // Calculate expiration time (e.g., 15 minutes from now)
        Instant expirationTime = Instant.now().plus(3, ChronoUnit.MINUTES);
        // Convert expiration time to ISO 8601 format
        String expirationTimeISO = DateTimeFormatter.ISO_INSTANT.format(expirationTime);
        String emailBody = String.format("Your New OTP for Username %s is\n%s \n(this otp will be valid for 3 min.)\nThank You For Registration.", user.getUsername(), randomOtp);
        mailService.sendMail(user.getEmail(), "VERIFY OTP FOR REGISTRATION", emailBody);
        otpVo.setExpireDate(expirationTime);
        otpRepository.save(otpVo);
        return Map.of("timeStamp", expirationTimeISO, "username", user.getUsername());
    }

    @Override
    public String getOtpExpriration(String username) {
        return "";
    }

    @Override
    public Map<?, ?> validateOtpForRegistration(OTPVo otpVo) {
        // Retrieve OTP and expiration time from the database
        List<OTPVo> otpVos = otpRepository.findByUser(otpVo.getUser());

        if (otpVos.isEmpty()) {
            return Map.of("message","Otp Not found");
        }

        OTPVo otpVoStored=otpVos.get(0);
        // Check if the OTP has expired
        Instant now = Instant.now();
        if (now.isAfter(otpVoStored.getExpireDate())) {
            return Map.of("message","otp expired");
        }

        // Check if the OTP matches
        if (!otpVo.getOtp().equals(otpVoStored.getOtp())) {
            return Map.of("message","invalid otp");
        }

        // OTP is valid and not expired
        otpRepository.delete(otpVoStored);
        return Map.of("message","valid");
    }
}
