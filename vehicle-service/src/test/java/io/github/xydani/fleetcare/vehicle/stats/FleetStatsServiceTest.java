package io.github.xydani.fleetcare.vehicle.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.xydani.fleetcare.vehicle.stats.dto.FleetStatsResponse.AgeBand;
import io.github.xydani.fleetcare.vehicle.stats.dto.FleetStatsResponse.BrandCount;
import io.github.xydani.fleetcare.vehicle.stats.dto.FleetStatsResponse.FuelTypeShare;
import io.github.xydani.fleetcare.vehicle.vehicle.FuelType;

@ExtendWith(MockitoExtension.class)
class FleetStatsServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 19);
    private static final ZoneId ROME = ZoneId.of("Europe/Rome");

    @Mock
    private FleetStatsQuery statsQuery;

    @Test
    void shouldSumFuelTypeCounts_asFleetTotal() {
        // given
        var service = new FleetStatsService(statsQuery, Clock.fixed(TODAY.atStartOfDay(ROME).toInstant(), ROME));
        when(statsQuery.countByFuelType()).thenReturn(List.of(
                new FuelTypeShare(FuelType.DIESEL, 3, new BigDecimal("60.0")),
                new FuelTypeShare(FuelType.PETROL, 2, new BigDecimal("40.0"))));
        when(statsQuery.countByAge(TODAY)).thenReturn(List.of(new AgeBand("0-3 years", 5, 12_000)));
        when(statsQuery.topBrands(5)).thenReturn(List.of(new BrandCount("Fiat", 5)));

        // when
        var stats = service.getStats();

        // then
        assertThat(stats.totalVehicles()).isEqualTo(5);
        assertThat(stats.byAge()).hasSize(1);
        assertThat(stats.topBrands()).extracting(BrandCount::brand).containsExactly("Fiat");
    }
}
