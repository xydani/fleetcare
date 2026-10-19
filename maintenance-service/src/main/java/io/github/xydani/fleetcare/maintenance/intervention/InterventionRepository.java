package io.github.xydani.fleetcare.maintenance.intervention;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InterventionRepository extends JpaRepository<Intervention, Long>,
        JpaSpecificationExecutor<Intervention> {

    /* Loads the booking in the same query: every response needs its vehicle and service type. */
    @Override
    @EntityGraph(attributePaths = "booking")
    Page<Intervention> findAll(Specification<Intervention> spec, Pageable pageable);

    @EntityGraph(attributePaths = "booking")
    Optional<Intervention> findWithBookingById(Long id);
}
