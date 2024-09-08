package com.maharshi.bollywood_game_spring_boot.utils;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class Utils {

    public  String generateOTP() {
        // Create a secure random number generator
        SecureRandom random = new SecureRandom();

        // Generate a random 6-digit number
        int otp = 100000 + random.nextInt(900000);

        // Convert the integer OTP to a string and return it
        return String.valueOf(otp);
    }
}
