package com.maharshi.bollywood_game_spring_boot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RedisService {

    @Autowired
    private RedisTemplate redisTemplate;

    public <T> T get(String key, Class<T> entityClass) {
        try {
            Object o = redisTemplate.opsForValue().get(key);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(o.toString(), entityClass);
        } catch (Exception e) {
            return null;
        }
    }

    public void set(String key, Object o, Long ttl) {
        try {
            ObjectMapper objectMapper=new ObjectMapper();
            String jsonValue=objectMapper.writeValueAsString(o);
            if (ttl == null || ttl <= 0) {
                // Set without expiration
                redisTemplate.opsForValue().set(key, jsonValue);
            } else {
                // Set with expiration
                redisTemplate.opsForValue().set(key, jsonValue, ttl, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.error("Exception",e);
        }
    }

    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Exception",e);
        }
    }
}
