package io.github.xydani.fleetcare.vehicle.stats;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.xydani.fleetcare.vehicle.stats.dto.FleetStatsResponse;

@RestController
public class FleetStatsController {

    private final FleetStatsService statsService;

    public FleetStatsController(FleetStatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/api/vehicles/stats")
    public FleetStatsResponse getStats() {
        return statsService.getStats();
    }
}
