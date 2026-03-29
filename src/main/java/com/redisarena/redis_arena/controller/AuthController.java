package com.redisarena.redis_arena.controller;

import com.redisarena.redis_arena.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Simulate login
    @PostMapping("/login")
    public String login(@RequestParam String userId) {
        return authService.login(userId);
    }

    // Use refresh token to get a new access token
    @PostMapping("/refresh")
    public String refresh(@RequestParam String refreshToken) {
        return authService.refresh(refreshToken);
    }

    // Logout and revoke refresh token
    @PostMapping("/logout")
    public String logout(@RequestParam String accessToken, @RequestParam String refreshToken) {
        authService.logout(accessToken, refreshToken);
        return "Logged out successfully";
    }
}