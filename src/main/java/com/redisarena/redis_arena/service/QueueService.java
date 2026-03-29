package com.redisarena.redis_arena.service;

import com.redisarena.redis_arena.model.QueueStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final RedisTemplate<String, String> redisTemplate;
    /*
    *
    * methods to interact with the redis Q
    *
    * admit(userId, eventId) - adds the user to the Q
    * getQueueStatus(userId, eventId) - gets the current user position
    * admitUsers(eventId) - method for the scheduler to call
    * activeUsers(eventId) - returns the currently active booking sessions
    *
    *
    * */

    private final int MAX_CAPACITY_PER_EVENT = 50;
    private final int ADMISSION_TTL_SECONDS = 600;

    public final String waitingQueueKey(String eventId) {
        return "waiting:queue:event:" + eventId;
    }

    public final String sessionKey(String userId, String eventId) {
        return "session:event:" + eventId + ":" + userId;
    }

    public final String sessionCounterKey(String eventId) {
        return "active:count:event:" + eventId;
    }

    public QueueStatus admit(String userId, String eventId) {
        String waitingQueueKey = waitingQueueKey(eventId);

        double score = Instant.now().toEpochMilli();
        redisTemplate.opsForZSet().addIfAbsent(waitingQueueKey, userId, score);
        return getQueueStatus(userId, eventId);
    }

    public QueueStatus getQueueStatus(String userId, String eventId) {
        // position 0 indicating that the user is admitted already.
        if(isAdmitted(userId, eventId)) return new QueueStatus(userId, eventId, 0, true);

        Long rank = redisTemplate.opsForZSet().rank(waitingQueueKey(eventId), userId);
        if(rank == null) throw new RuntimeException("User doesn't exist");

        return new QueueStatus(userId, eventId, rank + 1, false);
    }

    public int getActiveSessions(String eventId) {
        String activeCount = redisTemplate.opsForValue().get(sessionCounterKey(eventId));
        return activeCount == null ? 0 : Integer.parseInt(activeCount);
    }

    public boolean isAdmitted(String userId, String eventId) {
        Boolean admitted = redisTemplate.hasKey(sessionKey(userId, eventId));
        return Boolean.TRUE.equals(admitted);
    }

    public void admitNextUsers(String eventId) {
        int activeUsers = getActiveSessions(eventId);
        int availableSlots = MAX_CAPACITY_PER_EVENT - activeUsers;
        if(availableSlots <= 0) return;

        Set<ZSetOperations.TypedTuple<String>> nextUsers = redisTemplate.opsForZSet().popMin(
                waitingQueueKey(eventId),
                availableSlots
        );

        if(nextUsers == null || nextUsers.isEmpty()) return;

        for(ZSetOperations.TypedTuple<String> user: nextUsers) {
            String userId = user.getValue();
            if(userId == null) continue;

            redisTemplate.opsForValue().set(
                    sessionKey(user.getValue(), eventId),
                    "admitted",
                    ADMISSION_TTL_SECONDS,
                    TimeUnit.SECONDS
            );

            redisTemplate.opsForValue().increment(sessionCounterKey(eventId));
        }
    }

    public void completedBooking(String userId, String eventId) {
        redisTemplate.delete(sessionKey(userId, eventId));

        Long activeSessions = redisTemplate.opsForValue().decrement(sessionCounterKey(eventId));
        if(activeSessions != null && activeSessions < 0) {
            redisTemplate.opsForValue().set(sessionCounterKey(eventId), "0");
        }
    }

    public boolean isInQueue(String userId, String eventId) {
        // ZRANK returns null if member doesn't exist in the sorted set
        Long rank = redisTemplate.opsForZSet().rank(waitingQueueKey(eventId), userId);
        return rank != null;
    }
}
