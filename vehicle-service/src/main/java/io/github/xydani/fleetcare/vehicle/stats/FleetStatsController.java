package io.github.xydani.fleetcare.vehicle.stats;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import io.github.xydani.fleetcare.vehicle.stats.dto.FleetStatsResponse;

@RestController
@Tag(name = "Statistics", description = "Fleet-wide aggregates")
public class FleetStatsController {

    private final FleetStatsService statsService;

    public FleetStatsController(FleetStatsService statsService) {
        this.statsService = statsService;
    }

    @Operation(summary = "Fleet statistics",
            description = "Vehicles per fuel type, per age band with average km, and the most common brands.")
    @GetMapping("/api/vehicles/stats")
    public FleetStatsResponse getStats() {
        return statsService.getStats();
    }
}
