package io.github.xydani.fleetcare.vehicle.vehicle.dto;

import java.time.Instant;
import java.time.LocalDate;

import io.github.xydani.fleetcare.vehicle.vehicle.FuelType;

public record VehicleResponse(
        Long id,
        String plate,
        String brand,
        String model,
        Integer productionYear,
        FuelType fuelType,
        LocalDate registrationDate,
        OwnerSummary owner,
        Instant createdAt
) {
}
