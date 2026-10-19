package io.github.xydani.fleetcare.vehicle.inspection.dto;

import java.time.LocalDate;

/**
 * A vehicle whose next inspection is due soon or overdue.
 *
 * @param dueDate  expiry of the latest inspection, or registration date + 4 years if never inspected
 * @param daysLeft days until the due date, negative when overdue
 * @param overdue  true when the due date has already passed
 */
public record InspectionDue(
        Long vehicleId,
        String plate,
        String brand,
        String model,
        Long ownerId,
        String ownerName,
        String ownerEmail,
        LocalDate dueDate,
        Integer daysLeft,
        boolean overdue
) {
}
