package io.github.xydani.fleetcare.vehicle.vehicle;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VehicleRepository extends JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {

    /**
     * Loads the owner in the same query, so listing vehicles does not trigger one query per owner.
     */
    @Override
    @EntityGraph(attributePaths = "owner")
    Page<Vehicle> findAll(Specification<Vehicle> spec, Pageable pageable);

    @EntityGraph(attributePaths = "owner")
    Optional<Vehicle> findWithOwnerById(Long id);

    boolean existsByPlate(String plate);

    boolean existsByPlateAndIdNot(String plate, Long id);

    boolean existsByOwnerId(Long ownerId);
}
