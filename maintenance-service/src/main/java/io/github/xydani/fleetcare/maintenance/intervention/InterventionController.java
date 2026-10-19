package io.github.xydani.fleetcare.maintenance.intervention;

import java.net.URI;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import io.github.xydani.fleetcare.maintenance.common.web.PageResponse;
import io.github.xydani.fleetcare.maintenance.intervention.dto.CompletionRequest;
import io.github.xydani.fleetcare.maintenance.intervention.dto.InterventionResponse;
import io.github.xydani.fleetcare.maintenance.pricing.PricingPlan;

@RestController
@Tag(name = "Interventions", description = "Completed work and its cost")
public class InterventionController {

    private final InterventionService interventionService;

    public InterventionController(InterventionService interventionService) {
        this.interventionService = interventionService;
    }

    @Operation(summary = "List interventions",
            description = "Paginated, newest first. Filter by vehicle to get its maintenance history.")
    @GetMapping("/api/interventions")
    public PageResponse<InterventionResponse> findAll(
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) PricingPlan pricingPlan,
            @PageableDefault(size = 20, sort = "completedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return interventionService.findAll(vehicleId, pricingPlan, pageable);
    }

    @Operation(summary = "Get an intervention")
    @GetMapping("/api/interventions/{id}")
    public InterventionResponse findById(@PathVariable Long id) {
        return interventionService.findById(id);
    }

    @Operation(summary = "Complete a booking",
            description = "IN_PROGRESS -> COMPLETED. Records the intervention and prices it with the strategy "
                    + "of the chosen plan: STANDARD, WARRANTY (parts not billed) or FLEET (percentage discount).")
    @PostMapping("/api/bookings/{bookingId}/complete")
    public ResponseEntity<InterventionResponse> complete(@PathVariable Long bookingId,
                                                         @Valid @RequestBody CompletionRequest request) {
        InterventionResponse created = interventionService.complete(bookingId, request);
        return ResponseEntity.created(URI.create("/api/interventions/" + created.id())).body(created);
    }
}
