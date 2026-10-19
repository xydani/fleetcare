package io.github.xydani.fleetcare.maintenance.report;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import io.github.xydani.fleetcare.maintenance.report.dto.BrandAverageCost;
import io.github.xydani.fleetcare.maintenance.report.dto.MonthlySpending;
import io.github.xydani.fleetcare.maintenance.report.dto.TopIntervention;
import io.github.xydani.fleetcare.maintenance.report.dto.WeekdayLoad;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Maintenance costs and workshop load")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(summary = "Average intervention cost per brand",
            description = "Only brands with at least `minInterventions` interventions, most expensive first.")
    @GetMapping("/brands/average-cost")
    public List<BrandAverageCost> averageCostByBrand(
            @RequestParam(defaultValue = "3") @Min(1) int minInterventions) {
        return reportService.averageCostByBrand(minInterventions);
    }

    @Operation(summary = "Most expensive interventions per brand", description = "Ranked with RANK() OVER.")
    @GetMapping("/brands/top-interventions")
    public List<TopIntervention> topInterventionsByBrand(
            @RequestParam(defaultValue = "3") @Min(1) @Max(20) int limit) {
        return reportService.topInterventionsByBrand(limit);
    }

    @Operation(summary = "Monthly spending with running total",
            description = "All twelve months of the year (default: current year), empty months included.")
    @GetMapping("/monthly-spending")
    public List<MonthlySpending> monthlySpending(
            @RequestParam(required = false) @Min(2000) @Max(2100) Integer year) {
        return reportService.monthlySpending(year);
    }

    @Operation(summary = "Workshop load per day of the week",
            description = "Bookings and booked hours per weekday between two dates, both included "
                    + "(default: the last 90 days). Cancelled bookings are ignored.")
    @GetMapping("/workshop-load")
    public List<WeekdayLoad> workshopLoad(@RequestParam(required = false) LocalDate from,
                                          @RequestParam(required = false) LocalDate to) {
        return reportService.workshopLoad(from, to);
    }
}
