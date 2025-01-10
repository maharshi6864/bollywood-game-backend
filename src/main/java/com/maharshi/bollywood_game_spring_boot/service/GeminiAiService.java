package com.maharshi.bollywood_game_spring_boot.service;

public interface GeminiAiService {

    String askGemini(String prompt);

    String askChatGpt(String prompt);
}
