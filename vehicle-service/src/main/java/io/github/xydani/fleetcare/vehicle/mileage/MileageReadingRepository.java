package io.github.xydani.fleetcare.vehicle.mileage;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MileageReadingRepository extends JpaRepository<MileageReading, Long> {

    boolean existsByVehicleIdAndReadingDate(Long vehicleId, LocalDate readingDate);

    Optional<MileageReading> findFirstByVehicleIdOrderByReadingDateDesc(Long vehicleId);

    /** Latest reading strictly before the given date. */
    Optional<MileageReading> findFirstByVehicleIdAndReadingDateBeforeOrderByReadingDateDesc(
            Long vehicleId, LocalDate readingDate);

    /** Earliest reading strictly after the given date. */
    Optional<MileageReading> findFirstByVehicleIdAndReadingDateAfterOrderByReadingDateAsc(
            Long vehicleId, LocalDate readingDate);
}
