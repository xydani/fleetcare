package io.github.xydani.fleetcare.vehicle.mileage.dto;

import java.time.LocalDate;

public record MileageReadingResponse(Long id, Long vehicleId, LocalDate readingDate, Integer km) {
}
