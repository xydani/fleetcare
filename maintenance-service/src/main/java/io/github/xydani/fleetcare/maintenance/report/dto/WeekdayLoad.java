package io.github.xydani.fleetcare.maintenance.report.dto;

import java.math.BigDecimal;
import java.time.DayOfWeek;

/**
 * @param share percentage of all booked hours in the period that fall on this day
 */
public record WeekdayLoad(DayOfWeek dayOfWeek, long bookings, BigDecimal bookedHours, BigDecimal share) {
}
