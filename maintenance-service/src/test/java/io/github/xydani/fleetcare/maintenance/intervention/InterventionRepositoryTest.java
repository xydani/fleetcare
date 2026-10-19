package io.github.xydani.fleetcare.maintenance.intervention;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import io.github.xydani.fleetcare.maintenance.TestcontainersConfiguration;
import io.github.xydani.fleetcare.maintenance.booking.Booking;
import io.github.xydani.fleetcare.maintenance.booking.BookingRepository;
import io.github.xydani.fleetcare.maintenance.booking.ServiceType;
import io.github.xydani.fleetcare.maintenance.pricing.CostBreakdown;
import io.github.xydani.fleetcare.maintenance.pricing.PricingPlan;
import io.github.xydani.fleetcare.maintenance.vehicle.VehicleSnapshot;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class InterventionRepositoryTest {

    private static final LocalDateTime NINE = LocalDateTime.of(2026, 10, 26, 9, 0);

    @Autowired
    private InterventionRepository interventionRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void shouldFilterByVehicleAndPlan_andStoreTheCostBreakdown() {
        // given
        save(7L, NINE, PricingPlan.STANDARD);
        save(7L, NINE.plusDays(1), PricingPlan.FLEET);
        save(8L, NINE, PricingPlan.FLEET);

        // when
        var vehicleSeven = interventionRepository.findAll(
                InterventionSpecifications.withFilters(7L, null), PageRequest.of(0, 10));
        var fleetOfSeven = interventionRepository.findAll(
                InterventionSpecifications.withFilters(7L, PricingPlan.FLEET), PageRequest.of(0, 10));

        // then
        assertThat(vehicleSeven.getTotalElements()).isEqualTo(2);
        assertThat(fleetOfSeven.getContent()).singleElement().satisfies(intervention -> {
            assertThat(intervention.getBooking().getVehicle().plate()).isEqualTo("FG482KL");
            assertThat(intervention.getCost().totalCost()).isEqualByComparingTo("150.00");
        });
    }

    private void save(Long vehicleId, LocalDateTime startsAt, PricingPlan plan) {
        Booking booking = new Booking(vehicleId, new VehicleSnapshot("FG482KL", "Fiat", "Panda"),
                ServiceType.OIL_CHANGE, startsAt, startsAt.plusHours(1), null);
        booking.start();
        booking.complete();
        bookingRepository.save(booking);
        CostBreakdown cost = CostBreakdown.of(new BigDecimal("100"), new BigDecimal("50"), BigDecimal.ZERO);
        interventionRepository.saveAndFlush(new Intervention(booking, "Oil and filter", plan, BigDecimal.ONE, cost,
                startsAt.plusHours(1)));
    }
}
