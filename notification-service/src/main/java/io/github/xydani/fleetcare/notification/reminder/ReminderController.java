package io.github.xydani.fleetcare.notification.reminder;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import io.github.xydani.fleetcare.notification.reminder.dto.ReminderRun;

@RestController
@Tag(name = "Reminders", description = "Inspection reminders, also run daily by a scheduled job")
public class ReminderController {

    private final InspectionReminderJob inspectionReminderJob;

    public ReminderController(InspectionReminderJob inspectionReminderJob) {
        this.inspectionReminderJob = inspectionReminderJob;
    }

    @Operation(summary = "Run the inspection check now",
            description = "Same as the daily job. Safe to repeat: vehicles already reminded for the same due date "
                    + "are counted in alreadySent. 503 if the vehicle service is unavailable.")
    @PostMapping("/api/reminders/inspections")
    public ReminderRun runInspectionCheck() {
        return inspectionReminderJob.run();
    }
}
