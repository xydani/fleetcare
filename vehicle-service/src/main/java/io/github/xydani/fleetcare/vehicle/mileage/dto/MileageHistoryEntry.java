package io.github.xydani.fleetcare.vehicle.mileage.dto;

import java.time.LocalDate;

/**
 * One row of a vehicle's mileage history.
 *
 * @param kmSincePrevious    km driven since the previous reading, null for the first one
 * @param daysSincePrevious  days since the previous reading, null for the first one
 * @param movingAverageKm    average of kmSincePrevious over this and the two previous readings
 */
public record MileageHistoryEntry(
        LocalDate readingDate,
        Integer km,
        Integer kmSincePrevious,
        Integer daysSincePrevious,
        Integer movingAverageKm
) {
}
