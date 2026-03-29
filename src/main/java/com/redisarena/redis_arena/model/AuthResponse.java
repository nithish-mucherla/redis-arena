package com.redisarena.redis_arena.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    String accessToken;
    String refreshToken;
    String userId;
}
