package com.maharshi.bollywood_game_spring_boot.CustomException;

public class InvalidUserName extends RuntimeException {

    // Default constructor
    public InvalidUserName() {
        super("Invalid OTP");
    }

    // Constructor that accepts a custom message
    public InvalidUserName(String message) {
        super(message);
    }

    // Constructor that accepts a custom message and a cause
    public InvalidUserName(String message, Throwable cause) {
        super(message, cause);
    }

    // Constructor that accepts a cause
    public InvalidUserName(Throwable cause) {
        super(cause);
    }
}
