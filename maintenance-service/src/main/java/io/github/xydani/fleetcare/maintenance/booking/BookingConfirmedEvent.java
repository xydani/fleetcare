package io.github.xydani.fleetcare.maintenance.booking;

import java.time.LocalDateTime;

/**
 * Published when a booking is created. Delivered to listeners only once the transaction commits.
 */
public record BookingConfirmedEvent(Long bookingId, Long vehicleId, String plate, ServiceType serviceType,
                                    LocalDateTime startsAt) {
}
