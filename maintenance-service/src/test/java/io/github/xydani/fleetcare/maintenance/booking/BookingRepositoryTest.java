package io.github.xydani.fleetcare.maintenance.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import io.github.xydani.fleetcare.maintenance.TestcontainersConfiguration;
import io.github.xydani.fleetcare.maintenance.vehicle.VehicleSnapshot;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class BookingRepositoryTest {

    private static final LocalDateTime NINE = LocalDateTime.of(2026, 11, 2, 9, 0);

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void shouldCountOnlyActiveOverlappingBookings() {
        // given: 9-11 and 10-12 overlap the 10:30-11:30 window, 8-9 does not, the cancelled one is ignored
        save(1L, NINE, NINE.plusHours(2));
        save(2L, NINE.plusHours(1), NINE.plusHours(3));
        save(3L, NINE.minusHours(1), NINE);
        Booking cancelled = save(4L, NINE, NINE.plusHours(4));
        cancelled.cancel();
        bookingRepository.flush();

        // when
        long overlapping = bookingRepository.countOverlapping(
                NINE.plusMinutes(90), NINE.plusMinutes(150), BookingStatus.ACTIVE);

        // then
        assertThat(overlapping).isEqualTo(2);
    }

    @Test
    void shouldDetectOverlap_onlyForTheSameVehicle() {
        // given
        save(1L, NINE, NINE.plusHours(2));

        // when / then
        assertThat(bookingRepository.existsOverlappingForVehicle(
                1L, NINE.plusHours(1), NINE.plusHours(3), BookingStatus.ACTIVE)).isTrue();
        assertThat(bookingRepository.existsOverlappingForVehicle(
                2L, NINE.plusHours(1), NINE.plusHours(3), BookingStatus.ACTIVE)).isFalse();
        assertThat(bookingRepository.existsOverlappingForVehicle(
                1L, NINE.plusHours(2), NINE.plusHours(3), BookingStatus.ACTIVE)).isFalse();
    }

    @Test
    void shouldRejectOverlappingBookingForTheSameVehicle_atDatabaseLevel() {
        // given
        save(1L, NINE, NINE.plusHours(2));

        // when / then
        assertThatThrownBy(() -> bookingRepository.saveAndFlush(booking(1L, NINE.plusHours(1), NINE.plusHours(3))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldAllowRebooking_whenThePreviousBookingWasCancelled() {
        // given
        Booking first = save(1L, NINE, NINE.plusHours(2));
        first.cancel();
        bookingRepository.flush();

        // when
        Booking second = bookingRepository.saveAndFlush(booking(1L, NINE, NINE.plusHours(2)));

        // then
        assertThat(second.getId()).isNotNull();
        assertThat(second.getVehicle().plate()).isEqualTo("FG482KL");
    }

    private Booking save(Long vehicleId, LocalDateTime startsAt, LocalDateTime endsAt) {
        return bookingRepository.saveAndFlush(booking(vehicleId, startsAt, endsAt));
    }

    private static Booking booking(Long vehicleId, LocalDateTime startsAt, LocalDateTime endsAt) {
        return new Booking(vehicleId, new VehicleSnapshot("FG482KL", "Fiat", "Panda"),
                ServiceType.BRAKES, startsAt, endsAt, null);
    }
}
