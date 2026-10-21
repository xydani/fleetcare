package io.github.xydani.fleetcare.maintenance;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.util.ReflectionTestUtils;

import io.github.xydani.fleetcare.maintenance.booking.Booking;
import io.github.xydani.fleetcare.maintenance.booking.ServiceType;
import io.github.xydani.fleetcare.maintenance.intervention.Intervention;
import io.github.xydani.fleetcare.maintenance.pricing.CostBreakdown;
import io.github.xydani.fleetcare.maintenance.pricing.PricingPlan;
import io.github.xydani.fleetcare.maintenance.vehicle.VehicleSnapshot;

/**
 * Entities are equal when they have the same database id; a new entity (no id yet) equals only itself.
 * The hash code does not depend on the id, so an entity keeps its place in a HashSet when it is saved.
 */
class EntityEqualityTest {

    static Stream<Arguments> entities() {
        LocalDateTime nine = LocalDateTime.of(2026, 10, 26, 9, 0);
        Booking booking = booking(nine);
        CostBreakdown cost = CostBreakdown.of(BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ZERO);
        return Stream.of(
                entity("Booking", () -> booking(nine)),
                entity("Intervention", () -> new Intervention(booking, "Pads", PricingPlan.STANDARD, BigDecimal.ONE,
                        cost, nine.plusHours(2))));
    }

    private static Booking booking(LocalDateTime startsAt) {
        return new Booking(7L, new VehicleSnapshot("FG482KL", "Fiat", "Panda"), ServiceType.BRAKES, startsAt,
                startsAt.plusHours(2), null);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("entities")
    void shouldCompareByIdOnly(String name, Supplier<Object> factory) {
        Object first = withId(factory.get(), 1L);
        Object sameId = withId(factory.get(), 1L);
        Object otherId = withId(factory.get(), 2L);
        Object unsaved = factory.get();

        assertThat(first).isEqualTo(first).isEqualTo(sameId).isNotEqualTo(otherId).isNotEqualTo("not an entity");
        assertThat(unsaved).isEqualTo(unsaved).isNotEqualTo(factory.get()).isNotEqualTo(first);
        assertThat(first).hasSameHashCodeAs(unsaved);
    }

    private static Arguments entity(String name, Supplier<Object> factory) {
        return Arguments.of(name, factory);
    }

    private static Object withId(Object entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }
}
