package io.github.xydani.fleetcare.vehicle.mileage;

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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.xydani.fleetcare.vehicle.common.error.BusinessRuleException;
import io.github.xydani.fleetcare.vehicle.common.error.ConflictException;
import io.github.xydani.fleetcare.vehicle.mileage.dto.MileageReadingRequest;
import io.github.xydani.fleetcare.vehicle.owner.Owner;
import io.github.xydani.fleetcare.vehicle.vehicle.FuelType;
import io.github.xydani.fleetcare.vehicle.vehicle.Vehicle;
import io.github.xydani.fleetcare.vehicle.vehicle.VehicleService;

@ExtendWith(MockitoExtension.class)
class MileageServiceTest {

    private static final Long VEHICLE_ID = 1L;
    private static final LocalDate DATE = LocalDate.of(2026, 6, 1);

    @Mock
    private MileageReadingRepository readingRepository;

    @Mock
    private MileageHistoryQuery historyQuery;

    @Mock
    private VehicleService vehicleService;

    private MileageService mileageService;

    private final Vehicle vehicle = new Vehicle("FG482KL", "Fiat", "Panda", 2017, FuelType.PETROL,
            LocalDate.of(2017, 4, 12), new Owner("Giulia", "Bianchi", "giulia.bianchi@example.it", null));

    @BeforeEach
    void setUp() {
        mileageService = new MileageService(readingRepository, historyQuery, vehicleService);
        when(vehicleService.getVehicle(VEHICLE_ID)).thenReturn(vehicle);
    }

    @Test
    void shouldSaveReading_whenKmIsBetweenPreviousAndNextReadings() {
        // given
        givenPreviousReading(40_000);
        givenNextReading(46_000);
        when(readingRepository.save(any(MileageReading.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        var response = mileageService.addReading(VEHICLE_ID, new MileageReadingRequest(DATE, 43_000));

        // then
        assertThat(response.km()).isEqualTo(43_000);
        assertThat(response.readingDate()).isEqualTo(DATE);
    }

    @Test
    void shouldRejectReading_whenKmIsLowerThanPreviousReading() {
        // given
        givenPreviousReading(40_000);

        // when / then
        assertThatThrownBy(() -> mileageService.addReading(VEHICLE_ID, new MileageReadingRequest(DATE, 39_999)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("cannot decrease");
        verify(readingRepository, never()).save(any());
    }

    @Test
    void shouldRejectReading_whenKmIsHigherThanLaterReading() {
        // given
        givenPreviousReading(null);
        givenNextReading(46_000);

        // when / then
        assertThatThrownBy(() -> mileageService.addReading(VEHICLE_ID, new MileageReadingRequest(DATE, 46_001)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("later reading");
    }

    @Test
    void shouldRejectReading_whenDateIsAlreadyRecorded() {
        // given
        when(readingRepository.existsByVehicleIdAndReadingDate(VEHICLE_ID, DATE)).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> mileageService.addReading(VEHICLE_ID, new MileageReadingRequest(DATE, 43_000)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void shouldRejectReading_whenDateIsBeforeRegistration() {
        // when / then
        assertThatThrownBy(() -> mileageService.addReading(VEHICLE_ID,
                new MileageReadingRequest(LocalDate.of(2016, 1, 1), 10)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("registration date");
    }

    private void givenPreviousReading(Integer previousKm) {
        when(readingRepository.findFirstByVehicleIdAndReadingDateBeforeOrderByReadingDateDesc(VEHICLE_ID, DATE))
                .thenReturn(Optional.ofNullable(previousKm)
                        .map(km -> new MileageReading(vehicle, DATE.minusMonths(3), km)));
    }

    private void givenNextReading(Integer nextKm) {
        when(readingRepository.findFirstByVehicleIdAndReadingDateAfterOrderByReadingDateAsc(VEHICLE_ID, DATE))
                .thenReturn(Optional.ofNullable(nextKm)
                        .map(km -> new MileageReading(vehicle, DATE.plusMonths(3), km)));
    }
}
