package io.github.xydani.fleetcare.vehicle.mileage;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.xydani.fleetcare.vehicle.common.error.BusinessRuleException;
import io.github.xydani.fleetcare.vehicle.common.error.ConflictException;
import io.github.xydani.fleetcare.vehicle.mileage.dto.MileageHistoryEntry;
import io.github.xydani.fleetcare.vehicle.mileage.dto.MileageReadingRequest;
import io.github.xydani.fleetcare.vehicle.mileage.dto.MileageReadingResponse;
import io.github.xydani.fleetcare.vehicle.vehicle.Vehicle;
import io.github.xydani.fleetcare.vehicle.vehicle.VehicleService;

@Service
@Transactional(readOnly = true)
public class MileageService {

    private final MileageReadingRepository readingRepository;
    private final MileageHistoryQuery historyQuery;
    private final VehicleService vehicleService;

    public MileageService(MileageReadingRepository readingRepository, MileageHistoryQuery historyQuery,
                          VehicleService vehicleService) {
        this.readingRepository = readingRepository;
        this.historyQuery = historyQuery;
        this.vehicleService = vehicleService;
    }

    public List<MileageHistoryEntry> getHistory(Long vehicleId) {
        vehicleService.getVehicle(vehicleId);
        return historyQuery.findHistory(vehicleId);
    }

    /**
     * Records a reading. Readings may be added for past dates, so the value is checked against
     * both the reading before and the reading after: an odometer never goes backwards.
     */
    @Transactional
    public MileageReadingResponse addReading(Long vehicleId, MileageReadingRequest request) {
        Vehicle vehicle = vehicleService.getVehicle(vehicleId);

        if (request.readingDate().isBefore(vehicle.getRegistrationDate())) {
            throw new BusinessRuleException("Reading date cannot be before the vehicle registration date");
        }
        if (readingRepository.existsByVehicleIdAndReadingDate(vehicleId, request.readingDate())) {
            throw new ConflictException("A reading for " + request.readingDate() + " already exists");
        }

        readingRepository
                .findFirstByVehicleIdAndReadingDateBeforeOrderByReadingDateDesc(vehicleId, request.readingDate())
                .filter(previous -> request.km() < previous.getKm())
                .ifPresent(previous -> {
                    throw new BusinessRuleException("Mileage cannot decrease: " + previous.getKm()
                            + " km were recorded on " + previous.getReadingDate());
                });
        readingRepository
                .findFirstByVehicleIdAndReadingDateAfterOrderByReadingDateAsc(vehicleId, request.readingDate())
                .filter(next -> request.km() > next.getKm())
                .ifPresent(next -> {
                    throw new BusinessRuleException("Mileage cannot exceed the later reading of " + next.getKm()
                            + " km on " + next.getReadingDate());
                });

        MileageReading saved = readingRepository.save(
                new MileageReading(vehicle, request.readingDate(), request.km()));
        return new MileageReadingResponse(saved.getId(), vehicleId, saved.getReadingDate(), saved.getKm());
    }
}
