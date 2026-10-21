package io.github.xydani.fleetcare.notification.notification;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Derived queries: Spring Data builds the Mongo filter from each method name.
 */
public interface NotificationRepository extends MongoRepository<Notification, String> {

    Page<Notification> findByReadFalse(Pageable pageable);

    Page<Notification> findByVehicleId(Long vehicleId, Pageable pageable);

    Page<Notification> findByVehicleIdAndReadFalse(Long vehicleId, Pageable pageable);

    long countByReadFalse();

    Optional<Notification> findByDedupKey(String dedupKey);
}
