package io.github.xydani.fleetcare.vehicle.inspection;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import io.github.xydani.fleetcare.vehicle.inspection.dto.InspectionDue;
import io.github.xydani.fleetcare.vehicle.inspection.dto.InspectionRequest;
import io.github.xydani.fleetcare.vehicle.inspection.dto.InspectionResponse;

@RestController
@RequestMapping("/api/vehicles")
@Tag(name = "Inspections", description = "Periodic roadworthiness inspections (Italian \"revisione\")")
public class InspectionController {

    private final InspectionService inspectionService;

    public InspectionController(InspectionService inspectionService) {
        this.inspectionService = inspectionService;
    }

    @Operation(summary = "List a vehicle's inspections", description = "Most recent first.")
    @GetMapping("/{vehicleId}/inspections")
    public List<InspectionResponse> findByVehicle(@PathVariable Long vehicleId) {
        return inspectionService.findByVehicle(vehicleId);
    }

    @Operation(summary = "Record an inspection",
            description = "The expiry follows from the outcome: 2 years if passed, 1 month if to repeat, none if suspended.")
    @PostMapping("/{vehicleId}/inspections")
    @ResponseStatus(HttpStatus.CREATED)
    public InspectionResponse record(@PathVariable Long vehicleId, @Valid @RequestBody InspectionRequest request) {
        return inspectionService.record(vehicleId, request);
    }

    @Operation(summary = "Inspections due",
            description = "Vehicles whose inspection is overdue or expires within the given number of days.")
    @GetMapping("/inspections/expiring")
    public List<InspectionDue> findExpiring(@RequestParam(defaultValue = "30") @Min(0) @Max(365) int days) {
        return inspectionService.findDueWithin(days);
    }
}
