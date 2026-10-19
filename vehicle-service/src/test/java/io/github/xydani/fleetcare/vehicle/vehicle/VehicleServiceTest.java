package io.github.xydani.fleetcare.vehicle.vehicle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.xydani.fleetcare.vehicle.common.error.BusinessRuleException;
import io.github.xydani.fleetcare.vehicle.owner.Owner;
import io.github.xydani.fleetcare.vehicle.owner.OwnerRepository;
import io.github.xydani.fleetcare.vehicle.vehicle.dto.VehicleRequest;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private OwnerRepository ownerRepository;

    private VehicleService vehicleService;

    private final Owner owner = new Owner("Giulia", "Bianchi", "giulia.bianchi@example.it", null);

    @BeforeEach
    void setUp() {
        vehicleService = new VehicleService(vehicleRepository, ownerRepository, Mappers.getMapper(VehicleMapper.class));
    }

    @Test
    void shouldCreateVehicle_whenPlateIsFreeAndOwnerExists() {
        // given
        var request = request("FG482KL", 2017, LocalDate.of(2017, 4, 12));
        when(vehicleRepository.existsByPlate("FG482KL")).thenReturn(false);
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        var response = vehicleService.create(request);

        // then
        assertThat(response.plate()).isEqualTo("FG482KL");
        assertThat(response.owner().fullName()).isEqualTo("Giulia Bianchi");
    }

    @Test
    void shouldThrowDuplicatePlate_whenPlateIsAlreadyRegistered() {
        // given
        when(vehicleRepository.existsByPlate("FG482KL")).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> vehicleService.create(request("FG482KL", 2017, LocalDate.of(2017, 4, 12))))
                .isInstanceOf(DuplicatePlateException.class);
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void shouldRejectVehicle_whenRegisteredBeforeProductionYear() {
        // given
        when(vehicleRepository.existsByPlate("FG482KL")).thenReturn(false);

        // when / then
        assertThatThrownBy(() -> vehicleService.create(request("FG482KL", 2018, LocalDate.of(2017, 12, 31))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("production year");
    }

    @Test
    void shouldRejectVehicle_whenOwnerDoesNotExist() {
        // given
        when(vehicleRepository.existsByPlate("FG482KL")).thenReturn(false);
        when(ownerRepository.findById(1L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> vehicleService.create(request("FG482KL", 2017, LocalDate.of(2017, 4, 12))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Owner 1 does not exist");
    }

    @Test
    void shouldThrowNotFound_whenVehicleDoesNotExist() {
        // given
        when(vehicleRepository.findWithOwnerById(42L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> vehicleService.findById(42L))
                .isInstanceOf(VehicleNotFoundException.class)
                .hasMessage("Vehicle 42 not found");
    }

    @Test
    void shouldUpdateVehicleAndOwner_whenRequestIsValid() {
        // given
        var vehicle = new Vehicle("FG482KL", "Fiat", "Panda", 2017, FuelType.PETROL, LocalDate.of(2017, 4, 12),
                new Owner("Old", "Owner", "old@example.it", null));
        when(vehicleRepository.findWithOwnerById(5L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.existsByPlateAndIdNot("FG482KL", 5L)).thenReturn(false);
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));

        // when
        var response = vehicleService.update(5L, new VehicleRequest("FG482KL", "Fiat", "Panda Cross", 2017,
                FuelType.LPG, LocalDate.of(2017, 4, 12), 1L));

        // then
        assertThat(vehicle.getModel()).isEqualTo("Panda Cross");
        assertThat(vehicle.getFuelType()).isEqualTo(FuelType.LPG);
        assertThat(response.owner().fullName()).isEqualTo("Giulia Bianchi");
    }

    private static VehicleRequest request(String plate, int productionYear, LocalDate registrationDate) {
        return new VehicleRequest(plate, "Fiat", "Panda", productionYear, FuelType.PETROL, registrationDate, 1L);
    }
}
