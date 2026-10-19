package io.github.xydani.fleetcare.vehicle.owner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.xydani.fleetcare.vehicle.common.error.ConflictException;
import io.github.xydani.fleetcare.vehicle.owner.dto.OwnerRequest;
import io.github.xydani.fleetcare.vehicle.vehicle.VehicleRepository;

@ExtendWith(MockitoExtension.class)
class OwnerServiceTest {

    @Mock
    private OwnerRepository ownerRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    private OwnerService ownerService;

    private final OwnerRequest request =
            new OwnerRequest("Giulia", "Bianchi", "giulia.bianchi@example.it", "+39 333 1042231");

    @BeforeEach
    void setUp() {
        ownerService = new OwnerService(ownerRepository, vehicleRepository, Mappers.getMapper(OwnerMapper.class));
    }

    @Test
    void shouldCreateOwner_whenEmailIsFree() {
        // given
        when(ownerRepository.existsByEmailIgnoreCase(request.email())).thenReturn(false);
        when(ownerRepository.save(any(Owner.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        var response = ownerService.create(request);

        // then
        assertThat(response.firstName()).isEqualTo("Giulia");
        assertThat(response.email()).isEqualTo("giulia.bianchi@example.it");
    }

    @Test
    void shouldThrowDuplicateEmail_whenEmailIsTaken() {
        // given
        when(ownerRepository.existsByEmailIgnoreCase(request.email())).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> ownerService.create(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("giulia.bianchi@example.it");
        verify(ownerRepository, never()).save(any());
    }

    @Test
    void shouldThrowNotFound_whenOwnerDoesNotExist() {
        // given
        when(ownerRepository.findById(99L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> ownerService.findById(99L))
                .isInstanceOf(OwnerNotFoundException.class)
                .hasMessage("Owner 99 not found");
    }

    @Test
    void shouldUpdateEditableFields_whenOwnerExists() {
        // given
        Owner owner = new Owner("Old", "Name", "old@example.it", null);
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(ownerRepository.existsByEmailIgnoreCaseAndIdNot(request.email(), 1L)).thenReturn(false);

        // when
        var response = ownerService.update(1L, request);

        // then
        assertThat(owner.getLastName()).isEqualTo("Bianchi");
        assertThat(response.phone()).isEqualTo("+39 333 1042231");
    }

    @Test
    void shouldRefuseDelete_whenOwnerStillHasVehicles() {
        // given
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(new Owner("A", "B", "a@b.it", null)));
        when(vehicleRepository.existsByOwnerId(1L)).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> ownerService.delete(1L)).isInstanceOf(ConflictException.class);
        verify(ownerRepository, never()).delete(any());
    }
}
