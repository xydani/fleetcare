package io.github.xydani.fleetcare.maintenance.intervention;

import java.math.BigDecimal;

/**
 * Published when a booking is completed and its intervention recorded.
 * Delivered to listeners only once the transaction commits.
 */
public record InterventionCompletedEvent(Long interventionId, Long bookingId, Long vehicleId, String plate,
                                         BigDecimal totalCost) {
}
