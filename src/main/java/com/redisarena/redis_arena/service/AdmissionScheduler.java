package com.redisarena.redis_arena.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdmissionScheduler {
    private final QueueService queueService;

    @Scheduled(fixedDelay = 10000)
    public void admitUsers() {
        System.out.println("Running the admission scheduler");
        // hardcoded the event id but in prod this would iterate over all the events
        queueService.admitNextUsers("event:123");
    }
}
