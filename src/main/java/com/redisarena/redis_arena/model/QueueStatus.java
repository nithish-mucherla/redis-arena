package com.redisarena.redis_arena.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QueueStatus {
    String userId;
    String eventId;
    long position;
    boolean admitted;
}
