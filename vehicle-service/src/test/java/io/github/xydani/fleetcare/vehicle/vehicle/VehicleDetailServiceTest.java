package io.github.xydani.fleetcare.vehicle.vehicle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.github.xydani.fleetcare.vehicle.inspection.Inspection;
import io.github.xydani.fleetcare.vehicle.inspection.InspectionOutcome;
import io.github.xydani.fleetcare.vehicle.inspection.InspectionRepository;
import io.github.xydani.fleetcare.vehicle.mileage.MileageReading;
import io.github.xydani.fleetcare.vehicle.mileage.MileageReadingRepository;
import io.github.xydani.fleetcare.vehicle.owner.Owner;

@ExtendWith(MockitoExtension.class)
class VehicleDetailServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 19);
    private static final ZoneId ROME = ZoneId.of("Europe/Rome");

    @Mock
    private VehicleService vehicleService;

    @Mock
    private MileageReadingRepository readingRepository;

    @Mock
    private InspectionRepository inspectionRepository;

    private VehicleDetailService detailService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(TODAY.atStartOfDay(ROME).toInstant(), ROME);
        detailService = new VehicleDetailService(vehicleService, readingRepository, inspectionRepository,
                Mappers.getMapper(VehicleMapper.class), clock);
    }

    @Test
    void shouldCombineLatestReadingAndInspection_whenVehicleHasHistory() {
        // given
        Vehicle vehicle = vehicle(LocalDate.of(2017, 4, 12));
        when(vehicleService.getVehicle(1L)).thenReturn(vehicle);
        when(readingRepository.findFirstByVehicleIdOrderByReadingDateDesc(1L))
                .thenReturn(Optional.of(new MileageReading(vehicle, LocalDate.of(2026, 10, 1), 98_400)));
        when(inspectionRepository.findFirstByVehicleIdOrderByInspectionDateDesc(1L))
                .thenReturn(Optional.of(new Inspection(vehicle, LocalDate.of(2024, 11, 5),
                        LocalDate.of(2026, 11, 5), InspectionOutcome.PASSED, null)));

        // when
        var detail = detailService.getDetail(1L);

        // then
        assertThat(detail.owner().fullName()).isEqualTo("Giulia Bianchi");
        assertThat(detail.currentKm()).isEqualTo(98_400);
        assertThat(detail.lastInspectionOutcome()).isEqualTo(InspectionOutcome.PASSED);
        assertThat(detail.nextInspectionDue()).isEqualTo(LocalDate.of(2026, 11, 5));
        assertThat(detail.inspectionStatus()).isEqualTo(InspectionStatus.DUE_SOON);
    }

    @Test
    void shouldDueFirstInspectionFourYearsAfterRegistration_whenVehicleIsNew() {
        // given
        when(vehicleService.getVehicle(1L)).thenReturn(vehicle(LocalDate.of(2025, 3, 1)));
        when(readingRepository.findFirstByVehicleIdOrderByReadingDateDesc(1L)).thenReturn(Optional.empty());
        when(inspectionRepository.findFirstByVehicleIdOrderByInspectionDateDesc(1L)).thenReturn(Optional.empty());

        // when
        var detail = detailService.getDetail(1L);

        // then
        assertThat(detail.currentKm()).isNull();
        assertThat(detail.lastInspectionDate()).isNull();
        assertThat(detail.nextInspectionDue()).isEqualTo(LocalDate.of(2029, 3, 1));
        assertThat(detail.inspectionStatus()).isEqualTo(InspectionStatus.VALID);
    }

    private static Vehicle vehicle(LocalDate registrationDate) {
        Vehicle vehicle = new Vehicle("FG482KL", "Fiat", "Panda", registrationDate.getYear(), FuelType.PETROL,
                registrationDate, new Owner("Giulia", "Bianchi", "giulia.bianchi@example.it", null));
        ReflectionTestUtils.setField(vehicle, "id", 1L);
        return vehicle;
    }
}
