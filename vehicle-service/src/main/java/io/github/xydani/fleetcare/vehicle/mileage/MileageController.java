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

import io.github.xydani.fleetcare.vehicle.mileage.dto.MileageHistoryEntry;
import io.github.xydani.fleetcare.vehicle.mileage.dto.MileageReadingRequest;
import io.github.xydani.fleetcare.vehicle.mileage.dto.MileageReadingResponse;

@RestController
@RequestMapping("/api/vehicles/{vehicleId}/mileage")
public class MileageController {

    private final MileageService mileageService;

    public MileageController(MileageService mileageService) {
        this.mileageService = mileageService;
    }

    @GetMapping
    public List<MileageHistoryEntry> getHistory(@PathVariable Long vehicleId) {
        return mileageService.getHistory(vehicleId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MileageReadingResponse addReading(@PathVariable Long vehicleId,
                                             @Valid @RequestBody MileageReadingRequest request) {
        return mileageService.addReading(vehicleId, request);
    }
}
