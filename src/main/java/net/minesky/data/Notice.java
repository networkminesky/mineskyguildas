package net.minesky.data;

import java.time.Instant;

public class Notice {
    private final String message;
    private final long timestamp; // em millis

    public Notice(String message) {
        this.message = message;
        this.timestamp = Instant.now().toEpochMilli();
    }

    public Notice(String message, long timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return message;
    }
}
