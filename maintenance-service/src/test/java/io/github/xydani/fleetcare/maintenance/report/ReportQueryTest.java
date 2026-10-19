package io.github.xydani.fleetcare.maintenance.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import io.github.xydani.fleetcare.maintenance.TestcontainersConfiguration;
import io.github.xydani.fleetcare.maintenance.booking.Booking;
import io.github.xydani.fleetcare.maintenance.booking.BookingRepository;
import io.github.xydani.fleetcare.maintenance.booking.ServiceType;
import io.github.xydani.fleetcare.maintenance.intervention.Intervention;
import io.github.xydani.fleetcare.maintenance.intervention.InterventionRepository;
import io.github.xydani.fleetcare.maintenance.pricing.CostBreakdown;
import io.github.xydani.fleetcare.maintenance.pricing.PricingPlan;
import io.github.xydani.fleetcare.maintenance.report.dto.BrandAverageCost;
import io.github.xydani.fleetcare.maintenance.report.dto.TopIntervention;
import io.github.xydani.fleetcare.maintenance.report.dto.WeekdayLoad;
import io.github.xydani.fleetcare.maintenance.vehicle.VehicleSnapshot;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, ReportQuery.class})
class ReportQueryTest {

    private static final VehicleSnapshot PANDA = new VehicleSnapshot("FG482KL", "Fiat", "Panda");
    private static final VehicleSnapshot GOLF = new VehicleSnapshot("LM789NP", "Volkswagen", "Golf");

    @Autowired
    private ReportQuery reportQuery;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private InterventionRepository interventionRepository;

    @BeforeEach
    void setUp() {
        // Fiat: 100 + 300 + 300 (two Mondays, one Tuesday); Volkswagen: 500 (a Monday)
        completed(1L, PANDA, "2026-03-02T09:00", 2, "100.00");
        completed(1L, PANDA, "2026-03-10T09:00", 1, "300.00");
        completed(1L, PANDA, "2026-05-04T09:00", 1, "300.00");
        completed(2L, GOLF, "2026-05-11T09:00", 4, "500.00");
        Booking cancelled = booking(2L, GOLF, "2026-05-12T09:00", 8);
        cancelled.cancel();
        bookingRepository.flush();
    }

    @Test
    void shouldAverageCostPerBrand_onlyForBrandsWithEnoughInterventions() {
        assertThat(reportQuery.averageCostByBrand(1)).containsExactly(
                new BrandAverageCost("Volkswagen", 1, new BigDecimal("500.00"), new BigDecimal("500.00")),
                new BrandAverageCost("Fiat", 3, new BigDecimal("233.33"), new BigDecimal("700.00")));
        assertThat(reportQuery.averageCostByBrand(3))
                .extracting(BrandAverageCost::brand).containsExactly("Fiat");
    }

    @Test
    void shouldRankInterventionsWithinEachBrand_keepingTies() {
        var top = reportQuery.topInterventionsByBrand(1);

        // the two 300.00 Fiat interventions tie for first place
        assertThat(top).extracting(TopIntervention::brand, TopIntervention::costRank, TopIntervention::totalCost)
                .containsExactly(
                        tuple("Fiat", 1, new BigDecimal("300.00")),
                        tuple("Fiat", 1, new BigDecimal("300.00")),
                        tuple("Volkswagen", 1, new BigDecimal("500.00")));
    }

    @Test
    void shouldListAllMonthsWithRunningTotal() {
        var months = reportQuery.monthlySpending(2026);

        assertThat(months).hasSize(12);
        assertThat(months.get(0).month()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(months.get(0).spent()).isEqualByComparingTo("0");
        assertThat(months.get(2).interventions()).isEqualTo(2);
        assertThat(months.get(2).cumulativeSpent()).isEqualByComparingTo("400.00");
        assertThat(months.get(4).spent()).isEqualByComparingTo("800.00");
        assertThat(months.get(11).cumulativeSpent()).isEqualByComparingTo("1200.00");
    }

    @Test
    void shouldSplitBookedHoursByWeekday_ignoringCancelledBookings() {
        var load = reportQuery.workshopLoad(LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1));

        // Mondays: 2 + 1 + 4 = 7 hours, Tuesday: 1 hour
        assertThat(load).containsExactly(
                new WeekdayLoad(DayOfWeek.MONDAY, 3, new BigDecimal("7.00"), new BigDecimal("87.5")),
                new WeekdayLoad(DayOfWeek.TUESDAY, 1, new BigDecimal("1.00"), new BigDecimal("12.5")));
    }

    private void completed(Long vehicleId, VehicleSnapshot vehicle, String startsAt, int hours, String totalCost) {
        Booking booking = booking(vehicleId, vehicle, startsAt, hours);
        booking.start();
        booking.complete();
        CostBreakdown cost = CostBreakdown.of(new BigDecimal(totalCost), BigDecimal.ZERO, BigDecimal.ZERO);
        interventionRepository.saveAndFlush(new Intervention(booking, "Work", PricingPlan.STANDARD,
                BigDecimal.valueOf(hours), cost, booking.getEndsAt()));
    }

    private Booking booking(Long vehicleId, VehicleSnapshot vehicle, String startsAt, int hours) {
        LocalDateTime start = LocalDateTime.parse(startsAt);
        return bookingRepository.save(new Booking(vehicleId, vehicle, ServiceType.REPAIR, start,
                start.plusHours(hours), null));
    }
}
