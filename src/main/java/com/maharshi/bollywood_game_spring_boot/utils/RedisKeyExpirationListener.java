package com.maharshi.bollywood_game_spring_boot.utils;

import com.maharshi.bollywood_game_spring_boot.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisKeyExpirationListener {

    @Autowired
    private RedisService redisService;

    public void onMessage(String message) {
        log.info("Key expired: {}", message);

        // Example: Retrieve additional data if needed
        String data = fetchDataFromRedis(message);

        // Save to PostgreSQL
        if (data != null) {
            log.info("Data for expired key {} is received.", message);
        } else {
            log.warn("No data found for expired key {}", message);
        }
    }

    private String fetchDataFromRedis(String key) {
        Object object=redisService.get(key,Object.class);
        System.out.printf(String.valueOf(object.getClass()));

        return null; // Replace with your retrieval logic
    }
}
