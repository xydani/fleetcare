package io.github.xydani.fleetcare.notification.reminder;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * From {@code fleetcare.reminders.*}.
 *
 * @param cron      when the inspection check runs; {@code -} disables it
 * @param daysAhead how far ahead an inspection counts as "due soon"
 */
@Validated
@ConfigurationProperties("fleetcare.reminders")
public record ReminderProperties(@NotBlank String cron, @Min(0) @Max(365) int daysAhead) {
}
