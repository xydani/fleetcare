package io.github.xydani.fleetcare.notification.notification;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import io.github.xydani.fleetcare.notification.event.EventType;

/**
 * A message for the fleet manager or a vehicle owner, built from an event.
 */
@Document("notifications")
public class Notification {

    @Id
    private String id;

    private EventType type;

    @Field("vehicle_id")
    private Long vehicleId;

    private String plate;

    /** Owner's email when the event carries it, otherwise null (meant for the fleet manager). */
    private String recipient;

    private String title;

    private String message;

    private boolean read;

    @Field("created_at")
    private Instant createdAt;

    @Field("read_at")
    private Instant readAt;

    /**
     * Identifies a notification that must exist only once, e.g. one reminder per vehicle and due date.
     * Unique (sparse) index; null for notifications that may repeat.
     */
    @Field("dedup_key")
    private String dedupKey;

    protected Notification() {
        // required by Spring Data
    }

    public Notification(EventType type, Long vehicleId, String plate, String recipient, String title,
                        String message, String dedupKey, Instant createdAt) {
        this.type = type;
        this.vehicleId = vehicleId;
        this.plate = plate;
        this.recipient = recipient;
        this.title = title;
        this.message = message;
        this.dedupKey = dedupKey;
        this.createdAt = createdAt;
    }

    /**
     * Marks the notification as read. Reading it again keeps the first read time.
     */
    public void markRead(Instant now) {
        if (!read) {
            read = true;
            readAt = now;
        }
    }

    public String getId() {
        return id;
    }

    public EventType getType() {
        return type;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public String getPlate() {
        return plate;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return read;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public String getDedupKey() {
        return dedupKey;
    }
}
