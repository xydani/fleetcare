package io.github.xydani.fleetcare.vehicle.inspection;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.xydani.fleetcare.vehicle.common.error.BusinessRuleException;
import io.github.xydani.fleetcare.vehicle.inspection.dto.InspectionDue;
import io.github.xydani.fleetcare.vehicle.inspection.dto.InspectionRequest;
import io.github.xydani.fleetcare.vehicle.inspection.dto.InspectionResponse;
import io.github.xydani.fleetcare.vehicle.vehicle.Vehicle;
import io.github.xydani.fleetcare.vehicle.vehicle.VehicleService;

@Service
@Transactional(readOnly = true)
public class InspectionService {

    private static final Logger log = LoggerFactory.getLogger(InspectionService.class);

    private final InspectionRepository inspectionRepository;
    private final InspectionDueQuery dueQuery;
    private final InspectionMapper inspectionMapper;
    private final VehicleService vehicleService;
    private final Clock clock;

    public InspectionService(InspectionRepository inspectionRepository, InspectionDueQuery dueQuery,
                             InspectionMapper inspectionMapper, VehicleService vehicleService, Clock clock) {
        this.inspectionRepository = inspectionRepository;
        this.dueQuery = dueQuery;
        this.inspectionMapper = inspectionMapper;
        this.vehicleService = vehicleService;
        this.clock = clock;
    }

    public List<InspectionResponse> findByVehicle(Long vehicleId) {
        vehicleService.getVehicle(vehicleId);
        return inspectionRepository.findByVehicleIdOrderByInspectionDateDesc(vehicleId).stream()
                .map(inspectionMapper::toResponse)
                .toList();
    }

    /**
     * Records an inspection; the expiry date follows from the outcome.
     */
    @Transactional
    public InspectionResponse record(Long vehicleId, InspectionRequest request) {
        Vehicle vehicle = vehicleService.getVehicle(vehicleId);
        if (request.inspectionDate().isBefore(vehicle.getRegistrationDate())) {
            throw new BusinessRuleException("Inspection date cannot be before the vehicle registration date");
        }

        LocalDate expiry = request.outcome().expiryFrom(request.inspectionDate());
        Inspection inspection = new Inspection(vehicle, request.inspectionDate(), expiry,
                request.outcome(), request.notes());
        inspection = inspectionRepository.save(inspection);
        log.info("Inspection recorded for vehicle {}: {}, valid until {}", vehicleId, request.outcome(), expiry);
        return inspectionMapper.toResponse(inspection);
    }

    public List<InspectionDue> findDueWithin(int days) {
        return dueQuery.findDueWithin(days, LocalDate.now(clock));
    }
}
