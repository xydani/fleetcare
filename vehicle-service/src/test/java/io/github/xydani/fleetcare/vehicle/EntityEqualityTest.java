package io.github.xydani.fleetcare.vehicle;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.util.ReflectionTestUtils;

import io.github.xydani.fleetcare.vehicle.inspection.Inspection;
import io.github.xydani.fleetcare.vehicle.inspection.InspectionOutcome;
import io.github.xydani.fleetcare.vehicle.mileage.MileageReading;
import io.github.xydani.fleetcare.vehicle.owner.Owner;
import io.github.xydani.fleetcare.vehicle.vehicle.FuelType;
import io.github.xydani.fleetcare.vehicle.vehicle.Vehicle;

/**
 * Entities are equal when they have the same database id; a new entity (no id yet) equals only itself.
 * The hash code does not depend on the id, so an entity keeps its place in a HashSet when it is saved.
 */
class EntityEqualityTest {

    static Stream<Arguments> entities() {
        Owner owner = new Owner("Giulia", "Bianchi", "giulia@example.it", null);
        LocalDate registered = LocalDate.of(2017, 4, 12);
        Vehicle panda = new Vehicle("FG482KL", "Fiat", "Panda", 2017, FuelType.PETROL, registered, owner);
        LocalDate inspected = LocalDate.of(2025, 5, 2);
        return Stream.of(
                entity("Owner", () -> new Owner("Giulia", "Bianchi", "giulia@example.it", null)),
                entity("Vehicle", () -> new Vehicle("FG482KL", "Fiat", "Panda", 2017, FuelType.PETROL, registered,
                        owner)),
                entity("MileageReading", () -> new MileageReading(panda, LocalDate.of(2026, 1, 10), 21_000)),
                entity("Inspection", () -> new Inspection(panda, inspected, inspected.plusYears(2),
                        InspectionOutcome.PASSED, null)));
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
