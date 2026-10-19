package io.github.xydani.fleetcare.vehicle.inspection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.xydani.fleetcare.vehicle.common.error.BusinessRuleException;
import io.github.xydani.fleetcare.vehicle.inspection.dto.InspectionRequest;
import io.github.xydani.fleetcare.vehicle.inspection.dto.InspectionResponse;
import io.github.xydani.fleetcare.vehicle.owner.Owner;
import io.github.xydani.fleetcare.vehicle.vehicle.FuelType;
import io.github.xydani.fleetcare.vehicle.vehicle.Vehicle;
import io.github.xydani.fleetcare.vehicle.vehicle.VehicleService;

@ExtendWith(MockitoExtension.class)
class InspectionServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 19);

    @Mock
    private InspectionRepository inspectionRepository;

    @Mock
    private InspectionDueQuery dueQuery;

    @Mock
    private VehicleService vehicleService;

    private InspectionService inspectionService;

    private final Vehicle vehicle = new Vehicle("FG482KL", "Fiat", "Panda", 2017, FuelType.PETROL,
            LocalDate.of(2017, 4, 12), new Owner("Giulia", "Bianchi", "giulia.bianchi@example.it", null));

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(TODAY.atStartOfDay(ZoneId.of("Europe/Rome")).toInstant(), ZoneId.of("Europe/Rome"));
        inspectionService = new InspectionService(inspectionRepository, dueQuery,
                Mappers.getMapper(InspectionMapper.class), vehicleService, clock);
    }

    @Test
    void shouldSetExpiryTwoYearsLater_whenInspectionIsPassed() {
        // given
        when(vehicleService.getVehicle(1L)).thenReturn(vehicle);
        when(inspectionRepository.save(any(Inspection.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        var response = inspectionService.record(1L,
                new InspectionRequest(LocalDate.of(2026, 9, 1), InspectionOutcome.PASSED, null));

        // then
        assertThat(response.expiryDate()).isEqualTo(LocalDate.of(2028, 9, 1));
        assertThat(response.outcome()).isEqualTo(InspectionOutcome.PASSED);
    }

    @Test
    void shouldRejectInspection_whenDateIsBeforeRegistration() {
        // given
        when(vehicleService.getVehicle(1L)).thenReturn(vehicle);

        // when / then
        assertThatThrownBy(() -> inspectionService.record(1L,
                new InspectionRequest(LocalDate.of(2016, 1, 1), InspectionOutcome.PASSED, null)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void shouldListInspectionsMostRecentFirst_whenVehicleExists() {
        // given
        when(vehicleService.getVehicle(1L)).thenReturn(vehicle);
        when(inspectionRepository.findByVehicleIdOrderByInspectionDateDesc(1L)).thenReturn(List.of(
                new Inspection(vehicle, LocalDate.of(2025, 4, 1), LocalDate.of(2027, 4, 1),
                        InspectionOutcome.PASSED, null),
                new Inspection(vehicle, LocalDate.of(2023, 4, 1), LocalDate.of(2023, 5, 1),
                        InspectionOutcome.REPEAT, "Worn brake pads")));

        // when
        var inspections = inspectionService.findByVehicle(1L);

        // then
        assertThat(inspections).extracting(InspectionResponse::outcome)
                .containsExactly(InspectionOutcome.PASSED, InspectionOutcome.REPEAT);
    }

    @Test
    void shouldUseTodayFromClock_whenLookingForDueInspections() {
        // when
        inspectionService.findDueWithin(30);

        // then
        verify(dueQuery).findDueWithin(30, TODAY);
    }
}
