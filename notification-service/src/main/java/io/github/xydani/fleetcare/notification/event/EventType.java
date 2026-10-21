package io.github.xydani.fleetcare.notification.event;

/**
 * Events this service understands. Each one becomes a notification through the
 * {@link io.github.xydani.fleetcare.notification.notification.NotificationFactory}.
 */
public enum EventType {
    /** A vehicle's periodic inspection is overdue or expires soon. Raised by this service's own scheduler. */
    INSPECTION_DUE,
    /** The maintenance service booked a workshop slot. */
    BOOKING_CONFIRMED,
    /** The maintenance service completed and priced an intervention. */
    MAINTENANCE_COMPLETED
}
