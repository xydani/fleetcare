package io.github.xydani.fleetcare.maintenance.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @param month           first day of the month
 * @param cumulativeSpent spending from January up to and including this month
 */
public record MonthlySpending(LocalDate month, long interventions, BigDecimal spent, BigDecimal cumulativeSpent) {
}
