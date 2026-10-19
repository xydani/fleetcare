package io.github.xydani.fleetcare.vehicle.stats.dto;

import java.math.BigDecimal;
import java.util.List;

import io.github.xydani.fleetcare.vehicle.vehicle.FuelType;

public record FleetStatsResponse(
        long totalVehicles,
        List<FuelTypeShare> byFuelType,
        List<AgeBand> byAge,
        List<BrandCount> topBrands
) {

    /** Number of vehicles per fuel type and their share of the fleet, in percent. */
    public record FuelTypeShare(FuelType fuelType, long vehicles, BigDecimal percentage) {
    }

    /** Vehicles grouped by years since registration, with the average of their latest odometer reading. */
    public record AgeBand(String ageBand, long vehicles, Integer averageKm) {
    }

    public record BrandCount(String brand, long vehicles) {
    }
}
