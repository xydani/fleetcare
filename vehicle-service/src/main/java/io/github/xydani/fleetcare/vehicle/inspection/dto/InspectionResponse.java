package io.github.xydani.fleetcare.vehicle.inspection.dto;

import java.time.LocalDate;

import io.github.xydani.fleetcare.vehicle.inspection.InspectionOutcome;

public record InspectionResponse(
        Long id,
        Long vehicleId,
        LocalDate inspectionDate,
        LocalDate expiryDate,
        InspectionOutcome outcome,
        String notes
) {
}
