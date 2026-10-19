package io.github.xydani.fleetcare.vehicle.mileage;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import io.github.xydani.fleetcare.vehicle.TestcontainersConfiguration;
import io.github.xydani.fleetcare.vehicle.mileage.dto.MileageHistoryEntry;
import io.github.xydani.fleetcare.vehicle.owner.Owner;
import io.github.xydani.fleetcare.vehicle.owner.OwnerRepository;
import io.github.xydani.fleetcare.vehicle.vehicle.FuelType;
import io.github.xydani.fleetcare.vehicle.vehicle.Vehicle;
import io.github.xydani.fleetcare.vehicle.vehicle.VehicleRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, MileageHistoryQuery.class})
class MileageHistoryQueryTest {

    @Autowired
    private MileageHistoryQuery historyQuery;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private MileageReadingRepository readingRepository;

    @Test
    void shouldComputeDeltasAndMovingAverage_inDateOrder() {
        // given: readings saved out of order
        Owner owner = ownerRepository.save(new Owner("Giulia", "Bianchi", "giulia@example.it", null));
        Vehicle vehicle = vehicleRepository.save(new Vehicle("FG482KL", "Fiat", "Panda", 2017,
                FuelType.PETROL, LocalDate.of(2017, 4, 12), owner));
        save(vehicle, "2026-04-01", 13_000);
        save(vehicle, "2026-01-01", 10_000);
        save(vehicle, "2026-07-01", 14_500);
        save(vehicle, "2026-02-01", 11_000);
        readingRepository.flush();

        // when
        var history = historyQuery.findHistory(vehicle.getId());

        // then
        assertThat(history).extracting(MileageHistoryEntry::readingDate).isSorted();
        assertThat(history.get(0)).isEqualTo(
                new MileageHistoryEntry(LocalDate.parse("2026-01-01"), 10_000, null, null, null));
        assertThat(history.get(1).kmSincePrevious()).isEqualTo(1_000);
        assertThat(history.get(1).daysSincePrevious()).isEqualTo(31);
        assertThat(history.get(2).kmSincePrevious()).isEqualTo(2_000);
        // average of 1000, 2000 and 1500 over the last three deltas
        assertThat(history.get(3).movingAverageKm()).isEqualTo(1_500);
    }

    private void save(Vehicle vehicle, String date, int km) {
        readingRepository.save(new MileageReading(vehicle, LocalDate.parse(date), km));
    }
}
