package com.maharshi.bollywood_game_spring_boot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeminiAiServiceImp implements GeminiAiService {

    @Value("${gemini.api.url}")
    private String geminiAPIURL;

    @Value("${gemini.api.key}")
    private String geminiAPIKey;

    @Value("${chatgpt.api.key}")
    private String chatgptAPIKey;

    private final WebClient webClient;

    public GeminiAiServiceImp(WebClient.Builder webClient) {
        this.webClient = webClient.build();
    }

    @Override
    public String askGemini(String prompt) {
        // Corrected JSON structure based on the cURL command
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[] {
                        Map.of("parts", new Object[] {
                                Map.of("text", prompt)
                        })
                });

        try {
            // Updated WebClient call with proper structure
            return webClient.post()
                    .uri(geminiAPIURL + geminiAPIKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            // Add meaningful error handling
            throw new RuntimeException("Error calling Gemini API: " + e.getMessage(), e);
        }
    }

    @Override
    public String askChatGpt(String prompt) {

        // Corrected JSON structure based on the cURL command
        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o", // Specify the model
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", prompt)));

        try {
            // Updated WebClient call with proper structure
            return webClient.post()
                    .uri("https://api.openai.com/v1/chat/completions") // Ensure the URL is correct
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + chatgptAPIKey) // Set the Authorization header
                    .bodyValue(requestBody) // Add the request body
                    .retrieve()
                    .bodyToMono(String.class) // Parse the response as a String
                    .block(); // Blocking call to wait for the response
        } catch (Exception e) {
            // Add meaningful error handling
            throw new RuntimeException("Error calling Gemini API: " + e.getMessage(), e);
        }
    }

}
