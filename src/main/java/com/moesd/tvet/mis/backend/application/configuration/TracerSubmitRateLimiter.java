package com.moesd.tvet.mis.backend.application.configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

@Component
public class TracerSubmitRateLimiter {
	// Per-IP: 5 submissions per minute
	private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

	public Bucket resolve(String ip) {
		return buckets.computeIfAbsent(ip, k -> Bucket.builder()
				.addLimit(Bandwidth.builder().capacity(5).refillIntervally(5, Duration.ofMinutes(1)).build()).build());
	}
}
