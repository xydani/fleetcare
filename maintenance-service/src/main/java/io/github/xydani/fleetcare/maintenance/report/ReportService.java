package io.github.xydani.fleetcare.maintenance.report;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.xydani.fleetcare.maintenance.common.error.BusinessRuleException;
import io.github.xydani.fleetcare.maintenance.report.dto.BrandAverageCost;
import io.github.xydani.fleetcare.maintenance.report.dto.MonthlySpending;
import io.github.xydani.fleetcare.maintenance.report.dto.TopIntervention;
import io.github.xydani.fleetcare.maintenance.report.dto.WeekdayLoad;

@Service
@Transactional(readOnly = true)
public class ReportService {

    /** Default period of the workshop load report, today included. */
    private static final int DEFAULT_LOAD_DAYS = 90;

    private final ReportQuery reportQuery;
    private final Clock clock;

    public ReportService(ReportQuery reportQuery, Clock clock) {
        this.reportQuery = reportQuery;
        this.clock = clock;
    }

    public List<BrandAverageCost> averageCostByBrand(int minInterventions) {
        return reportQuery.averageCostByBrand(minInterventions);
    }

    public List<TopIntervention> topInterventionsByBrand(int limit) {
        return reportQuery.topInterventionsByBrand(limit);
    }

    /**
     * @param year the calendar year; the current one when null
     */
    public List<MonthlySpending> monthlySpending(Integer year) {
        return reportQuery.monthlySpending(year != null ? year : LocalDate.now(clock).getYear());
    }

    /**
     * Load between two days, both included. Defaults to the last 90 days up to today.
     */
    public List<WeekdayLoad> workshopLoad(LocalDate from, LocalDate to) {
        LocalDate end = to != null ? to : LocalDate.now(clock);
        LocalDate start = from != null ? from : end.minusDays(DEFAULT_LOAD_DAYS - 1);
        if (start.isAfter(end)) {
            throw new BusinessRuleException("'from' must not be after 'to'");
        }
        return reportQuery.workshopLoad(start, end.plusDays(1));
    }
}
