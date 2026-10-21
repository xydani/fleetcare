package io.github.xydani.fleetcare.notification.reminder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import feign.FeignException;

import io.github.xydani.fleetcare.notification.common.error.ServiceUnavailableException;
import io.github.xydani.fleetcare.notification.event.EventService;
import io.github.xydani.fleetcare.notification.event.EventType;
import io.github.xydani.fleetcare.notification.event.dto.EventRequest;
import io.github.xydani.fleetcare.notification.reminder.VehicleClient.InspectionDue;
import io.github.xydani.fleetcare.notification.reminder.dto.ReminderRun;

/**
 * Asks the vehicle service which inspections are overdue or due soon and turns each one into an
 * {@code INSPECTION_DUE} event. The event's deduplication key (vehicle + due date) makes the check safe to
 * repeat: running it every day sends one reminder per due date, not one per day.
 */
@Component
public class InspectionReminderJob {

    static final String SOURCE = "notification-service";

    private static final Logger log = LoggerFactory.getLogger(InspectionReminderJob.class);

    private final VehicleClient vehicleClient;
    private final EventService eventService;
    private final ReminderProperties properties;

    InspectionReminderJob(VehicleClient vehicleClient, EventService eventService, ReminderProperties properties) {
        this.vehicleClient = vehicleClient;
        this.eventService = eventService;
        this.properties = properties;
    }

    @Scheduled(cron = "${fleetcare.reminders.cron}")
    void runScheduled() {
        try {
            ReminderRun run = run();
            log.info("Inspection check: {} vehicles due, {} reminders created, {} already sent",
                    run.vehiclesDue(), run.created(), run.alreadySent());
        } catch (ServiceUnavailableException ex) {
            // nothing to retry here: tomorrow's run picks up the same vehicles
            log.warn("Inspection check skipped: {}", ex.getMessage());
        }
    }

    /**
     * @throws ServiceUnavailableException if the vehicle service cannot be reached
     */
    public ReminderRun run() {
        List<InspectionDue> due = fetchDue();
        int created = 0;
        for (InspectionDue inspection : due) {
            var event = new EventRequest(EventType.INSPECTION_DUE, SOURCE, payload(inspection));
            if (!eventService.ingest(event).duplicate()) {
                created++;
            }
        }
        return new ReminderRun(due.size(), created, due.size() - created);
    }

    private List<InspectionDue> fetchDue() {
        try {
            return vehicleClient.findExpiring(properties.daysAhead());
        } catch (FeignException ex) {
            throw new ServiceUnavailableException("Vehicle service is unavailable", ex);
        }
    }

    /* A HashMap because the owner's email may be missing, and Map.of rejects nulls. */
    private static Map<String, Object> payload(InspectionDue inspection) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("vehicleId", inspection.vehicleId());
        payload.put("plate", inspection.plate());
        payload.put("brand", inspection.brand());
        payload.put("model", inspection.model());
        payload.put("dueDate", inspection.dueDate().toString());
        payload.put("overdue", inspection.overdue());
        payload.put("ownerEmail", inspection.ownerEmail());
        return payload;
    }
}
