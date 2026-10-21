package io.github.xydani.fleetcare.notification.reminder;

import java.time.LocalDate;
import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Declarative HTTP client for the vehicle service's due-inspection lookup.
 */
@FeignClient(name = "vehicle-service", url = "${fleetcare.clients.vehicle-service-url}")
interface VehicleClient {

    @GetMapping("/api/vehicles/inspections/expiring")
    List<InspectionDue> findExpiring(@RequestParam("days") int days);

    /**
     * The fields this service needs; the rest of the response is ignored.
     */
    record InspectionDue(Long vehicleId, String plate, String brand, String model, String ownerEmail,
                         LocalDate dueDate, boolean overdue) {
    }
}
