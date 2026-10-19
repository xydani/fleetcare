package io.github.xydani.fleetcare.vehicle.mileage;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import io.github.xydani.fleetcare.vehicle.mileage.dto.MileageHistoryEntry;
import io.github.xydani.fleetcare.vehicle.mileage.dto.MileageReadingRequest;
import io.github.xydani.fleetcare.vehicle.mileage.dto.MileageReadingResponse;

@RestController
@RequestMapping("/api/vehicles/{vehicleId}/mileage")
@Tag(name = "Mileage", description = "Odometer readings")
public class MileageController {

    private final MileageService mileageService;

    public MileageController(MileageService mileageService) {
        this.mileageService = mileageService;
    }

    @Operation(summary = "Mileage history",
            description = "Readings in date order with km and days since the previous one and a 3-reading moving average.")
    @GetMapping
    public List<MileageHistoryEntry> getHistory(@PathVariable Long vehicleId) {
        return mileageService.getHistory(vehicleId);
    }

    @Operation(summary = "Add a reading",
            description = "One per day (409). Km must not be lower than an earlier reading or higher than a later one (422).")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MileageReadingResponse addReading(@PathVariable Long vehicleId,
                                             @Valid @RequestBody MileageReadingRequest request) {
        return mileageService.addReading(vehicleId, request);
    }
}
