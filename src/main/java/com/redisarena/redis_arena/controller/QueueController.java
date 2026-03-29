package com.redisarena.redis_arena.controller;

import com.redisarena.redis_arena.model.QueueStatus;
import com.redisarena.redis_arena.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;

    // Join the queue
    @PostMapping("/join")
    public QueueStatus join(@RequestParam String userId,
                            @RequestParam String eventId) {
        return queueService.admit(userId, eventId);
    }

    // Check position
    @GetMapping("/position")
    public QueueStatus position(@RequestParam String userId,
                                @RequestParam String eventId) {
        return queueService.getQueueStatus(userId, eventId);
    }

    // SSE stream — pushes position updates every 5 seconds
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam String userId,
                             @RequestParam String eventId) {

        if (!queueService.isInQueue(userId, eventId) &&
                !queueService.isAdmitted(userId, eventId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User has not joined the queue"
            );
        }

        // Timeout set to 10 minutes — max time a user waits
        SseEmitter emitter = new SseEmitter(600_000L);

        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                while (true) {
                    QueueStatus status = queueService.getQueueStatus(userId, eventId);
                    emitter.send(status);

                    // Stop streaming once admitted
                    if (status.isAdmitted()) {
                        emitter.complete();
                        break;
                    }

                    Thread.sleep(5000); // push update every 5 seconds
                }
            } catch (IOException | InterruptedException e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    // User done booking — free up the slot
    @PostMapping("/done")
    public String done(@RequestParam String userId,
                       @RequestParam String eventId) {
        queueService.completedBooking(userId, eventId);
        return "Slot released for: " + userId;
    }
}