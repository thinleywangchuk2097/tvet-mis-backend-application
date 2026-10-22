package com.moesd.tvet.mis.backend.application.configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

@Component
public class LoginRateLimiter {

    // Per-IP: 10 attempts per minute
    private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();

    // Per-account: 10 attempts per minute
    private final Map<String, Bucket> accountBuckets = new ConcurrentHashMap<>();

    public Bucket resolveIpBucket(String ip) {
        return ipBuckets.computeIfAbsent(ip, k -> newBucket(10, Duration.ofMinutes(1)));
    }

    public Bucket resolveAccountBucket(String username) {
        return accountBuckets.computeIfAbsent(username, k -> newBucket(10, Duration.ofMinutes(1)));
    }

    private Bucket newBucket(int capacity, Duration window) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillIntervally(capacity, window)
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}