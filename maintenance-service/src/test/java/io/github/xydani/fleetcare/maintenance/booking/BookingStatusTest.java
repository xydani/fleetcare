package io.github.xydani.fleetcare.maintenance.booking;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BookingStatusTest {

    @ParameterizedTest(name = "{0} -> {1}: {2}")
    @CsvSource({
            "BOOKED,      IN_PROGRESS, true",
            "BOOKED,      CANCELLED,   true",
            "BOOKED,      COMPLETED,   false",
            "IN_PROGRESS, COMPLETED,   true",
            "IN_PROGRESS, CANCELLED,   true",
            "IN_PROGRESS, BOOKED,      false",
            "COMPLETED,   CANCELLED,   false",
            "CANCELLED,   BOOKED,      false"
    })
    void shouldAllowOnlyLifecycleTransitions(BookingStatus from, BookingStatus to, boolean allowed) {
        assertThat(from.canMoveTo(to)).isEqualTo(allowed);
    }
}
