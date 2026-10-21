package io.github.xydani.fleetcare.notification.event.dto;

/**
 * @param notificationId the notification created, or the existing one when {@code duplicate} is true
 * @param duplicate      true when the same notification had already been created by an earlier event
 */
public record EventResponse(String eventId, String notificationId, boolean duplicate) {
}
