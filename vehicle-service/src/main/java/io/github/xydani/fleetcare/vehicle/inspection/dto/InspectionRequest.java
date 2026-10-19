package io.github.xydani.fleetcare.vehicle.inspection.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import io.github.xydani.fleetcare.vehicle.inspection.InspectionOutcome;

public record InspectionRequest(
        @NotNull @PastOrPresent LocalDate inspectionDate,
        @NotNull InspectionOutcome outcome,
        @Size(max = 500) String notes
) {
}
