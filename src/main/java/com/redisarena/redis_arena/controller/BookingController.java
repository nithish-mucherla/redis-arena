package com.redisarena.redis_arena.controller;

import com.redisarena.redis_arena.model.QueueStatus;
import com.redisarena.redis_arena.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final QueueService queueService;

    @GetMapping("/page")
    public QueueStatus bookingAccess(@RequestParam String userId, @RequestParam String eventId) {
        if (!queueService.isAdmitted(userId, eventId)) {
            return queueService.admit(userId, eventId);
        }

        return new QueueStatus(userId, eventId, 0, true);
    }
}
