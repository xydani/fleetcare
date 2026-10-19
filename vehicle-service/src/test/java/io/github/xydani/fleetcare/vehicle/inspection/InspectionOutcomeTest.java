package io.github.xydani.fleetcare.vehicle.inspection;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class InspectionOutcomeTest {

    @ParameterizedTest
    @CsvSource({
            "PASSED,    2026-03-15, 2028-03-15",
            "REPEAT,    2026-03-15, 2026-04-15",
            "SUSPENDED, 2026-03-15, 2026-03-16",
            "PASSED,    2024-02-29, 2026-02-28"
    })
    void shouldComputeExpiry_fromInspectionDateAndOutcome(InspectionOutcome outcome, LocalDate inspectionDate,
                                                          LocalDate expectedExpiry) {
        assertThat(outcome.expiryFrom(inspectionDate)).isEqualTo(expectedExpiry);
    }
}
