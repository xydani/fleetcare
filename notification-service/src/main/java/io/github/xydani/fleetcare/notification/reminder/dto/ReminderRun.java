package io.github.xydani.fleetcare.notification.reminder.dto;

/**
 * Outcome of one inspection check.
 *
 * @param vehiclesDue vehicles the vehicle service reported as due
 * @param created     new reminders
 * @param alreadySent vehicles that already had a reminder for the same due date
 */
public record ReminderRun(int vehiclesDue, int created, int alreadySent) {
}
