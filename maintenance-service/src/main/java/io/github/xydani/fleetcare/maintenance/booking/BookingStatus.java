package io.github.xydani.fleetcare.maintenance.booking;

import java.util.Set;

/**
 * Booking lifecycle: {@code BOOKED -> IN_PROGRESS -> COMPLETED}, with {@code CANCELLED}
 * reachable until the work is completed. COMPLETED and CANCELLED are final.
 */
public enum BookingStatus {
    BOOKED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    /** Statuses that hold a slot in the workshop. */
    public static final Set<BookingStatus> ACTIVE = Set.of(BOOKED, IN_PROGRESS);

    public boolean canMoveTo(BookingStatus next) {
        return switch (this) {
            case BOOKED -> next == IN_PROGRESS || next == CANCELLED;
            case IN_PROGRESS -> next == COMPLETED || next == CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
    }
}
