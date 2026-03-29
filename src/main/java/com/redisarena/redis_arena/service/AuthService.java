package com.redisarena.redis_arena.service;

import com.redisarena.redis_arena.model.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final int REFRESH_TOKEN_TTL_IN_DAYS = 7;

    public String login(String userId) {
        String refreshToken = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set(
                "refresh:token:" + refreshToken,
                userId,
                REFRESH_TOKEN_TTL_IN_DAYS,
                TimeUnit.DAYS
        );

        return refreshToken;
    }

    public String refresh(String refreshToken) {
        String userId = redisTemplate.opsForValue().get("refresh:token:" + refreshToken);

        if(userId == null) throw new RuntimeException("Invalid/expired refresh token");

        // issue a new JWT access token - simulating using a String
        return "access-token-for:"+ userId + ":" + UUID.randomUUID();
    }

    public void logout(String accessToken, String refreshToken) {
        String userId = redisTemplate.opsForValue().get("refresh:token:" + refreshToken);
        if(userId == null) {
            throw new RuntimeException("Invalid/expired refresh token");
        }

        long remainingTtlInDays = 1;
        redisTemplate.opsForValue().set(
                "deny:token:" + accessToken,
                "revoked",
                remainingTtlInDays,
                TimeUnit.DAYS
        );
        redisTemplate.delete("refresh:token:" + refreshToken);
    }
}
