package io.github.xydani.fleetcare.vehicle.inspection;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import io.github.xydani.fleetcare.vehicle.TestcontainersConfiguration;
import io.github.xydani.fleetcare.vehicle.inspection.dto.InspectionDue;
import io.github.xydani.fleetcare.vehicle.owner.Owner;
import io.github.xydani.fleetcare.vehicle.owner.OwnerRepository;
import io.github.xydani.fleetcare.vehicle.vehicle.FuelType;
import io.github.xydani.fleetcare.vehicle.vehicle.Vehicle;
import io.github.xydani.fleetcare.vehicle.vehicle.VehicleRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, InspectionDueQuery.class})
class InspectionDueQueryTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 19);

    @Autowired
    private InspectionDueQuery dueQuery;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private InspectionRepository inspectionRepository;

    private Owner owner;

    @BeforeEach
    void setUp() {
        owner = ownerRepository.save(new Owner("Giulia", "Bianchi", "giulia@example.it", null));
    }

    @Test
    void shouldUseLatestInspectionOnly_whenVehicleHasHistory() {
        // given: an old inspection expired long ago, the latest one expires in 10 days
        Vehicle vehicle = vehicle("AB123CD", LocalDate.of(2015, 1, 10));
        inspect(vehicle, LocalDate.of(2022, 10, 29), InspectionOutcome.PASSED);
        inspect(vehicle, LocalDate.of(2024, 10, 29), InspectionOutcome.PASSED);

        // when
        var due = dueQuery.findDueWithin(30, TODAY);

        // then
        assertThat(due).singleElement().satisfies(entry -> {
            assertThat(entry.plate()).isEqualTo("AB123CD");
            assertThat(entry.dueDate()).isEqualTo(LocalDate.of(2026, 10, 29));
            assertThat(entry.daysLeft()).isEqualTo(10);
            assertThat(entry.overdue()).isFalse();
            assertThat(entry.ownerName()).isEqualTo("Giulia Bianchi");
        });
    }

    @Test
    void shouldIncludeOverdueAndExcludeFarFutureInspections() {
        // given
        inspect(vehicle("AB123CD", LocalDate.of(2015, 1, 10)), LocalDate.of(2024, 9, 1), InspectionOutcome.PASSED);
        inspect(vehicle("EF456GH", LocalDate.of(2015, 1, 10)), LocalDate.of(2026, 1, 15), InspectionOutcome.PASSED);

        // when
        var due = dueQuery.findDueWithin(30, TODAY);

        // then
        assertThat(due).extracting(InspectionDue::plate).containsExactly("AB123CD");
        assertThat(due.get(0).overdue()).isTrue();
        assertThat(due.get(0).daysLeft()).isNegative();
    }

    @Test
    void shouldTreatNeverInspectedVehicle_asDueFourYearsAfterRegistration() {
        // given: registered 2022-11-01, never inspected, first inspection due 2026-11-01
        vehicle("LM789NP", LocalDate.of(2022, 11, 1));
        vehicle("PR012ST", LocalDate.of(2025, 5, 1));

        // when
        var due = dueQuery.findDueWithin(30, TODAY);

        // then
        assertThat(due).extracting(InspectionDue::plate).containsExactly("LM789NP");
        assertThat(due.get(0).dueDate()).isEqualTo(LocalDate.of(2026, 11, 1));
    }

    private Vehicle vehicle(String plate, LocalDate registrationDate) {
        return vehicleRepository.save(new Vehicle(plate, "Fiat", "Panda", registrationDate.getYear(),
                FuelType.PETROL, registrationDate, owner));
    }

    private void inspect(Vehicle vehicle, LocalDate date, InspectionOutcome outcome) {
        inspectionRepository.saveAndFlush(new Inspection(vehicle, date, outcome.expiryFrom(date), outcome, null));
    }
}
