package io.github.xydani.fleetcare.maintenance.booking;

import java.time.Duration;

/**
 * Kinds of work the workshop takes bookings for, each with the slot length used when the
 * customer does not ask for a specific one.
 */
public enum ServiceType {
    OIL_CHANGE(Duration.ofMinutes(45)),
    TYRE_CHANGE(Duration.ofHours(1)),
    BRAKES(Duration.ofHours(2)),
    SCHEDULED_SERVICE(Duration.ofHours(3)),
    INSPECTION_PREP(Duration.ofHours(1)),
    REPAIR(Duration.ofHours(4));

    private final Duration defaultDuration;

    ServiceType(Duration defaultDuration) {
        this.defaultDuration = defaultDuration;
    }

    public Duration defaultDuration() {
        return defaultDuration;
    }
}
