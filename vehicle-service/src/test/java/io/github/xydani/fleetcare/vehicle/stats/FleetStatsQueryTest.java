package io.github.xydani.fleetcare.vehicle.stats;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import io.github.xydani.fleetcare.vehicle.TestcontainersConfiguration;
import io.github.xydani.fleetcare.vehicle.mileage.MileageReading;
import io.github.xydani.fleetcare.vehicle.mileage.MileageReadingRepository;
import io.github.xydani.fleetcare.vehicle.owner.Owner;
import io.github.xydani.fleetcare.vehicle.owner.OwnerRepository;
import io.github.xydani.fleetcare.vehicle.stats.dto.FleetStatsResponse.AgeBand;
import io.github.xydani.fleetcare.vehicle.stats.dto.FleetStatsResponse.BrandCount;
import io.github.xydani.fleetcare.vehicle.stats.dto.FleetStatsResponse.FuelTypeShare;
import io.github.xydani.fleetcare.vehicle.vehicle.FuelType;
import io.github.xydani.fleetcare.vehicle.vehicle.Vehicle;
import io.github.xydani.fleetcare.vehicle.vehicle.VehicleRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, FleetStatsQuery.class})
class FleetStatsQueryTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 19);

    @Autowired
    private FleetStatsQuery statsQuery;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private MileageReadingRepository readingRepository;

    private Owner owner;

    @BeforeEach
    void setUp() {
        owner = ownerRepository.save(new Owner("Giulia", "Bianchi", "giulia@example.it", null));
        // two recent petrol Fiats, one old diesel Volkswagen, one recent electric Tesla
        Vehicle panda = vehicle("AB123CD", "Fiat", FuelType.PETROL, LocalDate.of(2024, 1, 10));
        vehicle("EF456GH", "Fiat", FuelType.PETROL, LocalDate.of(2023, 6, 1));
        Vehicle golf = vehicle("LM789NP", "Volkswagen", FuelType.DIESEL, LocalDate.of(2013, 3, 5));
        vehicle("PR012ST", "Tesla", FuelType.ELECTRIC, LocalDate.of(2025, 2, 20));

        reading(panda, "2025-01-10", 10_000);
        reading(panda, "2026-01-10", 21_000);
        reading(golf, "2026-05-01", 190_000);
        readingRepository.flush();
    }

    @Test
    void shouldCountVehiclesAndShares_perFuelType() {
        var shares = statsQuery.countByFuelType();

        assertThat(shares).containsExactly(
                new FuelTypeShare(FuelType.PETROL, 2, new BigDecimal("50.0")),
                new FuelTypeShare(FuelType.DIESEL, 1, new BigDecimal("25.0")),
                new FuelTypeShare(FuelType.ELECTRIC, 1, new BigDecimal("25.0")));
    }

    @Test
    void shouldGroupByAgeBand_withAverageOfLatestReadings() {
        var bands = statsQuery.countByAge(TODAY);

        // the 0-3 band averages only the vehicle with readings, using its latest one (21,000 km)
        assertThat(bands).containsExactly(
                new AgeBand("0-3 years", 3, 21_000),
                new AgeBand("12+ years", 1, 190_000));
    }

    @Test
    void shouldListMostCommonBrandsFirst() {
        assertThat(statsQuery.topBrands(2)).containsExactly(
                new BrandCount("Fiat", 2),
                new BrandCount("Tesla", 1));
    }

    private Vehicle vehicle(String plate, String brand, FuelType fuelType, LocalDate registrationDate) {
        return vehicleRepository.save(new Vehicle(plate, brand, "Model", registrationDate.getYear(), fuelType,
                registrationDate, owner));
    }

    private void reading(Vehicle vehicle, String date, int km) {
        readingRepository.save(new MileageReading(vehicle, LocalDate.parse(date), km));
    }
}
