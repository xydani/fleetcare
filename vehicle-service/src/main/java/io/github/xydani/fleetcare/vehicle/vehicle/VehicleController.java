package io.github.xydani.fleetcare.vehicle.vehicle;

import java.net.URI;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import io.github.xydani.fleetcare.vehicle.common.web.PageResponse;
import io.github.xydani.fleetcare.vehicle.vehicle.dto.VehicleDetailResponse;
import io.github.xydani.fleetcare.vehicle.vehicle.dto.VehicleRequest;
import io.github.xydani.fleetcare.vehicle.vehicle.dto.VehicleResponse;

@RestController
@RequestMapping("/api/vehicles")
@Tag(name = "Vehicles", description = "Vehicle registry")
public class VehicleController {

    private final VehicleService vehicleService;
    private final VehicleDetailService vehicleDetailService;

    public VehicleController(VehicleService vehicleService, VehicleDetailService vehicleDetailService) {
        this.vehicleService = vehicleService;
        this.vehicleDetailService = vehicleDetailService;
    }

    @Operation(summary = "List vehicles",
            description = "Paginated, optionally filtered by brand (case-insensitive), fuel type and owner.")
    @GetMapping
    public PageResponse<VehicleResponse> findAll(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) FuelType fuelType,
            @RequestParam(required = false) Long ownerId,
            @PageableDefault(size = 20, sort = "plate", direction = Sort.Direction.ASC) Pageable pageable) {
        return vehicleService.findAll(brand, fuelType, ownerId, pageable);
    }

    @Operation(summary = "Get a vehicle",
            description = "Includes the owner, the latest odometer reading and the inspection status.")
    @GetMapping("/{id}")
    public VehicleDetailResponse findById(@PathVariable Long id) {
        return vehicleDetailService.getDetail(id);
    }

    @Operation(summary = "Register a vehicle",
            description = "Italian plate format, unique (409). The owner must exist (422).")
    @PostMapping
    public ResponseEntity<VehicleResponse> create(@Valid @RequestBody VehicleRequest request) {
        VehicleResponse created = vehicleService.create(request);
        return ResponseEntity.created(URI.create("/api/vehicles/" + created.id())).body(created);
    }

    @Operation(summary = "Update a vehicle")
    @PutMapping("/{id}")
    public VehicleResponse update(@PathVariable Long id, @Valid @RequestBody VehicleRequest request) {
        return vehicleService.update(id, request);
    }

    @Operation(summary = "Delete a vehicle", description = "Also deletes its mileage readings and inspections.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
