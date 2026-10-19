package io.github.xydani.fleetcare.vehicle.inspection;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InspectionRepository extends JpaRepository<Inspection, Long> {

    List<Inspection> findByVehicleIdOrderByInspectionDateDesc(Long vehicleId);

    Optional<Inspection> findFirstByVehicleIdOrderByInspectionDateDesc(Long vehicleId);
}
