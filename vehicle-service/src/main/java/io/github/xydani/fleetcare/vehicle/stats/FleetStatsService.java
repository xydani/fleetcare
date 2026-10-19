package io.github.xydani.fleetcare.vehicle.stats;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.xydani.fleetcare.vehicle.stats.dto.FleetStatsResponse;
import io.github.xydani.fleetcare.vehicle.stats.dto.FleetStatsResponse.FuelTypeShare;

@Service
@Transactional(readOnly = true)
public class FleetStatsService {

    private static final int TOP_BRANDS = 5;

    private final FleetStatsQuery statsQuery;
    private final Clock clock;

    public FleetStatsService(FleetStatsQuery statsQuery, Clock clock) {
        this.statsQuery = statsQuery;
        this.clock = clock;
    }

    public FleetStatsResponse getStats() {
        var byFuelType = statsQuery.countByFuelType();
        long total = byFuelType.stream().mapToLong(FuelTypeShare::vehicles).sum();
        return new FleetStatsResponse(total, byFuelType,
                statsQuery.countByAge(LocalDate.now(clock)), statsQuery.topBrands(TOP_BRANDS));
    }
}
