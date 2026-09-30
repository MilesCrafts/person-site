package com.lekang.journal.message.application;

import com.lekang.journal.common.error.TooManyRequestsException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MessageRateLimiter {
    private static final int MAX_KEYS = 10_000;
    private static final int MAX_MESSAGES = 3;
    private static final Duration WINDOW = Duration.ofMinutes(30);
    private final Map<String, Window> windows = new LinkedHashMap<>(32, 0.75f, true);
    private final Clock clock;

    public MessageRateLimiter() {
        this(Clock.systemUTC());
    }

    MessageRateLimiter(Clock clock) {
        this.clock = clock;
    }

    public synchronized void consume(String key) {
        purgeExpired();
        Instant now = clock.instant();
        Window current = windows.get(key);
        if (current != null && current.count >= MAX_MESSAGES) {
            throw new TooManyRequestsException("too many messages; try again later");
        }
        if (current == null) {
            if (windows.size() >= MAX_KEYS) {
                Iterator<String> iterator = windows.keySet().iterator();
                if (iterator.hasNext()) {
                    iterator.next();
                    iterator.remove();
                }
            }
            windows.put(key, new Window(1, now.plus(WINDOW)));
        } else {
            windows.put(key, new Window(current.count + 1, current.expiresAt));
        }
    }

    private void purgeExpired() {
        Instant now = clock.instant();
        windows.entrySet().removeIf(entry -> !entry.getValue().expiresAt.isAfter(now));
    }

    private record Window(int count, Instant expiresAt) {
    }
}
