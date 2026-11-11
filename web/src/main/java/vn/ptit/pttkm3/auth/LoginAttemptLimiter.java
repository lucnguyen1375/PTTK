package vn.ptit.pttkm3.auth;

import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;

public final class LoginAttemptLimiter {
    private static final long WINDOW_MILLIS = Duration.ofMinutes(15).toMillis();
    private static final int MAX_FAILURES = 5;
    private final ConcurrentHashMap<Key, Bucket> attempts = new ConcurrentHashMap<>();

    public long blockedSeconds(String username, String ip, long nowMillis) {
        Key key = key(username, ip);
        AtomicLong remaining = new AtomicLong();
        attempts.compute(key, (ignored, bucket) -> {
            if (bucket == null) return null;
            synchronized (bucket) {
                prune(bucket, nowMillis);
                if (bucket.failures.isEmpty()) return null;
                if (bucket.failures.size() >= MAX_FAILURES) {
                    remaining.set(Math.max(1, (bucket.failures.peekFirst() + WINDOW_MILLIS - nowMillis + 999) / 1000));
                }
                return bucket;
            }
        });
        return remaining.get();
    }

    public void recordFailure(String username, String ip, long nowMillis) {
        Key key = key(username, ip);
        attempts.compute(key, (ignored, bucket) -> {
            if (bucket == null) bucket = new Bucket();
            synchronized (bucket) {
                prune(bucket, nowMillis);
                bucket.failures.addLast(nowMillis);
                return bucket;
            }
        });
    }

    public void clear(String username, String ip) {
        attempts.remove(key(username, ip));
    }

    private static void prune(Bucket bucket, long now) {
        while (!bucket.failures.isEmpty() && now - bucket.failures.peekFirst() >= WINDOW_MILLIS) {
            bucket.failures.removeFirst();
        }
    }

    private static Key key(String username, String ip) {
        String canonical = Normalizer.normalize(username.strip(), Normalizer.Form.NFC).toLowerCase(java.util.Locale.ROOT);
        return new Key(canonical, ip);
    }

    private record Key(String username, String ip) { }
    private static final class Bucket { private final ArrayDeque<Long> failures = new ArrayDeque<>(); }
}
