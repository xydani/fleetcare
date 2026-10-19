package io.github.xydani.fleetcare.maintenance.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import io.github.xydani.fleetcare.maintenance.booking.dto.BookingRequest;
import io.github.xydani.fleetcare.maintenance.common.error.BusinessRuleException;
import io.github.xydani.fleetcare.maintenance.common.error.ConflictException;
import io.github.xydani.fleetcare.maintenance.vehicle.VehicleLookup;
import io.github.xydani.fleetcare.maintenance.vehicle.VehicleNotFoundException;
import io.github.xydani.fleetcare.maintenance.vehicle.VehicleSnapshot;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    /* Monday 19 October 2026, 10:00 in Italy. */
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-19T08:00:00Z"), ZoneId.of("Europe/Rome"));
    private static final LocalDateTime NEXT_MONDAY_NINE = LocalDateTime.of(2026, 10, 26, 9, 0);
    private static final VehicleSnapshot PANDA = new VehicleSnapshot("FG482KL", "Fiat", "Panda");

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private VehicleLookup vehicleLookup;

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        var workshop = new WorkshopProperties(2, LocalTime.of(8, 0), LocalTime.of(18, 0));
        bookingService = new BookingService(bookingRepository, vehicleLookup, Mappers.getMapper(BookingMapper.class),
                workshop, CLOCK);
    }

    @Test
    void shouldBookDefaultLengthSlot_whenDurationIsMissing() {
        // given
        givenVehicleExists();
        when(bookingRepository.saveAndFlush(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        var response = bookingService.create(request(NEXT_MONDAY_NINE, null));

        // then: brakes take two hours by default
        assertThat(response.endsAt()).isEqualTo(NEXT_MONDAY_NINE.plusHours(2));
        assertThat(response.plate()).isEqualTo("FG482KL");
        assertThat(response.status()).isEqualTo(BookingStatus.BOOKED);
    }

    @Test
    void shouldUseRequestedDuration_whenPresent() {
        // given
        givenVehicleExists();
        when(bookingRepository.saveAndFlush(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        var response = bookingService.create(request(NEXT_MONDAY_NINE, 30));

        // then
        assertThat(response.endsAt()).isEqualTo(NEXT_MONDAY_NINE.plusMinutes(30));
    }

    @ParameterizedTest(name = "{0} for {1} min: {2}")
    @CsvSource({
            "2026-10-19T09:00, 60,  must start in the future",
            "2026-10-24T09:00, 60,  closed at weekends",
            "2026-10-26T07:30, 60,  opening hours",
            "2026-10-26T17:30, 60,  opening hours",
            "2026-10-26T17:00, 600, opening hours"
    })
    void shouldRejectSlot_whenOutsideOpeningHours(LocalDateTime startsAt, int minutes, String reason) {
        assertThatThrownBy(() -> bookingService.create(request(startsAt, minutes)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining(reason);
        verifyNoInteractions(vehicleLookup, bookingRepository);
    }

    @Test
    void shouldPropagateNotFound_whenVehicleDoesNotExist() {
        // given
        when(vehicleLookup.snapshotOf(7L)).thenThrow(new VehicleNotFoundException(7L));

        // when / then
        assertThatThrownBy(() -> bookingService.create(request(NEXT_MONDAY_NINE, null)))
                .isInstanceOf(VehicleNotFoundException.class);
        verify(bookingRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldRejectBooking_whenVehicleIsAlreadyBookedAtThatTime() {
        // given
        givenVehicleExists();
        when(bookingRepository.existsOverlappingForVehicle(eq(7L), any(), any(), any())).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> bookingService.create(request(NEXT_MONDAY_NINE, null)))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Vehicle 7 already has a booking at that time");
    }

    @Test
    void shouldRejectBooking_whenAllBaysAreTaken() {
        // given
        givenVehicleExists();
        when(bookingRepository.countOverlapping(any(), any(), any())).thenReturn(2L);

        // when / then
        assertThatThrownBy(() -> bookingService.create(request(NEXT_MONDAY_NINE, null)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("No free bay");
        verify(bookingRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldReportConflict_whenConcurrentBookingHitsTheDatabaseConstraint() {
        // given
        givenVehicleExists();
        when(bookingRepository.saveAndFlush(any(Booking.class)))
                .thenThrow(new DataIntegrityViolationException("ex_booking_vehicle_overlap"));

        // when / then
        assertThatThrownBy(() -> bookingService.create(request(NEXT_MONDAY_NINE, null)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void shouldMoveThroughLifecycle_whenStartedThenCancelled() {
        // given
        Booking booking = new Booking(7L, PANDA, ServiceType.BRAKES, NEXT_MONDAY_NINE, NEXT_MONDAY_NINE.plusHours(2),
                null);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        // when
        var started = bookingService.start(1L);
        var cancelled = bookingService.cancel(1L);

        // then
        assertThat(started.status()).isEqualTo(BookingStatus.IN_PROGRESS);
        assertThat(cancelled.status()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void shouldRejectTransition_whenBookingIsAlreadyCancelled() {
        // given
        Booking booking = new Booking(7L, PANDA, ServiceType.BRAKES, NEXT_MONDAY_NINE, NEXT_MONDAY_NINE.plusHours(2),
                null);
        booking.cancel();
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        // when / then
        assertThatThrownBy(() -> bookingService.start(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("cannot move from CANCELLED to IN_PROGRESS");
    }

    @Test
    void shouldThrowNotFound_whenBookingDoesNotExist() {
        // given
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> bookingService.findById(99L))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessage("Booking 99 not found");
    }

    private void givenVehicleExists() {
        when(vehicleLookup.snapshotOf(7L)).thenReturn(PANDA);
    }

    private static BookingRequest request(LocalDateTime startsAt, Integer durationMinutes) {
        return new BookingRequest(7L, ServiceType.BRAKES, startsAt, durationMinutes, null);
    }
}
