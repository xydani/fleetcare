package io.github.xydani.fleetcare.vehicle.vehicle;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.xydani.fleetcare.vehicle.inspection.Inspection;
import io.github.xydani.fleetcare.vehicle.inspection.InspectionRepository;
import io.github.xydani.fleetcare.vehicle.mileage.MileageReadingRepository;
import io.github.xydani.fleetcare.vehicle.vehicle.dto.VehicleDetailResponse;

/**
 * Assembles the vehicle detail view from the vehicle, its latest reading and its latest inspection.
 */
@Service
@Transactional(readOnly = true)
public class VehicleDetailService {

    /** Italian rule: first inspection four years after registration. */
    private static final int FIRST_INSPECTION_AFTER_YEARS = 4;

    private final VehicleService vehicleService;
    private final MileageReadingRepository readingRepository;
    private final InspectionRepository inspectionRepository;
    private final VehicleMapper vehicleMapper;
    private final Clock clock;

    public VehicleDetailService(VehicleService vehicleService, MileageReadingRepository readingRepository,
                                InspectionRepository inspectionRepository, VehicleMapper vehicleMapper,
                                Clock clock) {
        this.vehicleService = vehicleService;
        this.readingRepository = readingRepository;
        this.inspectionRepository = inspectionRepository;
        this.vehicleMapper = vehicleMapper;
        this.clock = clock;
    }

    public VehicleDetailResponse getDetail(Long id) {
        Vehicle vehicle = vehicleService.getVehicle(id);

        var builder = VehicleDetailResponse.builder()
                .id(vehicle.getId())
                .plate(vehicle.getPlate())
                .brand(vehicle.getBrand())
                .model(vehicle.getModel())
                .productionYear(vehicle.getProductionYear())
                .fuelType(vehicle.getFuelType())
                .registrationDate(vehicle.getRegistrationDate())
                .owner(vehicleMapper.toOwnerSummary(vehicle.getOwner()));

        readingRepository.findFirstByVehicleIdOrderByReadingDateDesc(id)
                .ifPresent(reading -> builder.mileage(reading.getKm(), reading.getReadingDate()));

        Optional<Inspection> lastInspection = inspectionRepository.findFirstByVehicleIdOrderByInspectionDateDesc(id);
        lastInspection.ifPresent(inspection ->
                builder.lastInspection(inspection.getInspectionDate(), inspection.getOutcome()));

        LocalDate dueDate = lastInspection
                .map(Inspection::getExpiryDate)
                .orElse(vehicle.getRegistrationDate().plusYears(FIRST_INSPECTION_AFTER_YEARS));
        builder.nextInspection(dueDate, InspectionStatus.of(dueDate, LocalDate.now(clock)));

        return builder.build();
    }
}
