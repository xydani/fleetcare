package io.github.xydani.fleetcare.maintenance.report;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.xydani.fleetcare.maintenance.common.error.BusinessRuleException;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-19T08:00:00Z"), ZoneId.of("Europe/Rome"));

    @Mock
    private ReportQuery reportQuery;

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(reportQuery, CLOCK);
    }

    @Test
    void shouldUseCurrentYear_whenYearIsMissing() {
        reportService.monthlySpending(null);

        verify(reportQuery).monthlySpending(2026);
    }

    @Test
    void shouldUseLast90DaysIncludingToday_whenDatesAreMissing() {
        reportService.workshopLoad(null, null);

        verify(reportQuery).workshopLoad(LocalDate.of(2026, 7, 22), LocalDate.of(2026, 10, 20));
    }

    @Test
    void shouldIncludeTheLastDay_whenDatesAreGiven() {
        reportService.workshopLoad(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        verify(reportQuery).workshopLoad(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 1));
    }

    @Test
    void shouldRejectRange_whenFromIsAfterTo() {
        assertThatThrownBy(() -> reportService.workshopLoad(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1)))
                .isInstanceOf(BusinessRuleException.class);
    }
}
