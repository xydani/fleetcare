package io.github.xydani.fleetcare.vehicle.mileage;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MileageReadingRepository extends JpaRepository<MileageReading, Long> {
}
