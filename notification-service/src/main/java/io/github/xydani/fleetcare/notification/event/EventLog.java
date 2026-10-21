package io.github.xydani.fleetcare.notification.event;

import java.time.Instant;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Every event received, as it arrived. The payload has a different shape for each type, which is
 * why this lives in a document store. Expired after 90 days by a TTL index on {@code receivedAt}.
 */
@Document("event_log")
public class EventLog {

    @Id
    private String id;

    private EventType type;

    private String source;

    private Map<String, Object> payload;

    @Field("received_at")
    private Instant receivedAt;

    protected EventLog() {
        // required by Spring Data
    }

    public EventLog(EventType type, String source, Map<String, Object> payload, Instant receivedAt) {
        this.type = type;
        this.source = source;
        this.payload = payload;
        this.receivedAt = receivedAt;
    }

    public String getId() {
        return id;
    }

    public EventType getType() {
        return type;
    }

    public String getSource() {
        return source;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }
}
