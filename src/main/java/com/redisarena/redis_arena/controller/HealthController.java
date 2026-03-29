package com.redisarena.redis_arena.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HealthController {

    private final RedisTemplate<String, String> redisTemplate;

    @GetMapping
    public String health() {
        redisTemplate.opsForValue().set("health:check", "ok");
        String value = redisTemplate.opsForValue().get("health:check");
        return "Redis says " + value;
    }

}
