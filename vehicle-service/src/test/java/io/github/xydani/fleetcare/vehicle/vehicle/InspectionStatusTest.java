package io.github.xydani.fleetcare.vehicle.vehicle;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class InspectionStatusTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 19);

    @ParameterizedTest
    @CsvSource({
            "2026-10-18, OVERDUE",
            "2026-10-19, DUE_SOON",
            "2026-11-18, DUE_SOON",
            "2026-11-19, VALID"
    })
    void shouldClassifyDueDate_relativeToToday(LocalDate dueDate, InspectionStatus expected) {
        assertThat(InspectionStatus.of(dueDate, TODAY)).isEqualTo(expected);
    }
}
