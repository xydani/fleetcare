package io.github.xydani.fleetcare.notification.notification.dto;

import java.time.Instant;

import io.github.xydani.fleetcare.notification.event.EventType;
import io.github.xydani.fleetcare.notification.notification.Notification;

public record NotificationResponse(
        String id,
        EventType type,
        Long vehicleId,
        String plate,
        String recipient,
        String title,
        String message,
        boolean read,
        Instant createdAt,
        Instant readAt
) {

    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(n.getId(), n.getType(), n.getVehicleId(), n.getPlate(), n.getRecipient(),
                n.getTitle(), n.getMessage(), n.isRead(), n.getCreatedAt(), n.getReadAt());
    }
}
