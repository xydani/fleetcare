package io.github.xydani.fleetcare.maintenance.booking;

import java.time.LocalDateTime;
import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    /* Two ranges overlap when each one starts before the other ends; touching slots (10-11, 11-12) do not. */
    @Query("""
            select count(b) from Booking b
            where b.status in :statuses and b.startsAt < :endsAt and b.endsAt > :startsAt
            """)
    long countOverlapping(@Param("startsAt") LocalDateTime startsAt,
                          @Param("endsAt") LocalDateTime endsAt,
                          @Param("statuses") Collection<BookingStatus> statuses);

    @Query("""
            select count(b) > 0 from Booking b
            where b.vehicleId = :vehicleId
              and b.status in :statuses and b.startsAt < :endsAt and b.endsAt > :startsAt
            """)
    boolean existsOverlappingForVehicle(@Param("vehicleId") Long vehicleId,
                                        @Param("startsAt") LocalDateTime startsAt,
                                        @Param("endsAt") LocalDateTime endsAt,
                                        @Param("statuses") Collection<BookingStatus> statuses);
}
