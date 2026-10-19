package io.github.xydani.fleetcare.vehicle.vehicle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import io.github.xydani.fleetcare.vehicle.TestcontainersConfiguration;
import io.github.xydani.fleetcare.vehicle.owner.Owner;
import io.github.xydani.fleetcare.vehicle.owner.OwnerRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class VehicleRepositoryTest {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    private Owner owner;

    @BeforeEach
    void setUp() {
        owner = ownerRepository.save(new Owner("Mario", "Rossi", "mario.rossi@example.com", null));
    }

    @Test
    void shouldFindVehicleByPlate_whenVehicleExists() {
        // given
        vehicleRepository.save(newVehicle("AB123CD"));

        // when
        var found = vehicleRepository.findByPlate("AB123CD");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getOwner().getEmail()).isEqualTo("mario.rossi@example.com");
        assertThat(found.get().getCreatedAt()).isNotNull();
    }

    @Test
    void shouldReportPlateAsTaken_whenVehicleWithSamePlateExists() {
        // given
        vehicleRepository.save(newVehicle("AB123CD"));

        // when / then
        assertThat(vehicleRepository.existsByPlate("AB123CD")).isTrue();
        assertThat(vehicleRepository.existsByPlate("ZZ999ZZ")).isFalse();
    }

    @Test
    void shouldRejectDuplicatePlate_whenSavingSecondVehicle() {
        // given
        vehicleRepository.saveAndFlush(newVehicle("AB123CD"));

        // when / then
        assertThatThrownBy(() -> vehicleRepository.saveAndFlush(newVehicle("AB123CD")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Vehicle newVehicle(String plate) {
        return new Vehicle(plate, "Fiat", "Panda", 2019, FuelType.PETROL,
                LocalDate.of(2019, 5, 10), owner);
    }
}
