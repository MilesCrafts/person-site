package com.lekang.journal.admin.application;

import com.lekang.journal.common.error.TooManyRequestsException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class LoginRateLimiter {
    private static final int MAX_KEYS = 10_000;
    private static final int MAX_FAILURES = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);

    private final Map<String, Attempt> attempts = new LinkedHashMap<>(32, 0.75f, true);
    private final Clock clock;

    public LoginRateLimiter() {
        this(Clock.systemUTC());
    }

    LoginRateLimiter(Clock clock) {
        this.clock = clock;
    }

    public synchronized void checkAllowed(String key) {
        purgeExpired();
        Attempt attempt = attempts.get(key);
        if (attempt != null && attempt.failures >= MAX_FAILURES) {
            throw new TooManyRequestsException("too many login attempts; try again later");
        }
    }

    public synchronized void recordFailure(String key) {
        purgeExpired();
        Instant now = clock.instant();
        Attempt existing = attempts.get(key);
        if (existing == null) {
            if (attempts.size() >= MAX_KEYS) {
                Iterator<String> iterator = attempts.keySet().iterator();
                if (iterator.hasNext()) {
                    iterator.next();
                    iterator.remove();
                }
            }
            attempts.put(key, new Attempt(1, now.plus(WINDOW)));
        } else {
            attempts.put(key, new Attempt(existing.failures + 1, existing.expiresAt));
        }
    }

    public synchronized void reset(String key) {
        attempts.remove(key);
    }

    private void purgeExpired() {
        Instant now = clock.instant();
        attempts.entrySet().removeIf(entry -> !entry.getValue().expiresAt.isAfter(now));
    }

    private record Attempt(int failures, Instant expiresAt) {
    }
}
