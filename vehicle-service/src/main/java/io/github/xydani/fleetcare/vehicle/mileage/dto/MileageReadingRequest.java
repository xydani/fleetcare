package io.github.xydani.fleetcare.vehicle.mileage.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;

public record MileageReadingRequest(
        @NotNull @PastOrPresent LocalDate readingDate,
        @NotNull @PositiveOrZero @Max(2_000_000) Integer km
) {
}
