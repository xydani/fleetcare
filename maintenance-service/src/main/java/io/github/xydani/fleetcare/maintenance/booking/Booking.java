package io.github.xydani.fleetcare.maintenance.booking;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import io.github.xydani.fleetcare.maintenance.common.error.BusinessRuleException;
import io.github.xydani.fleetcare.maintenance.vehicle.VehicleSnapshot;

@Entity
@Table(name = "booking")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Id in the vehicle service: a plain value, not a foreign key, because that table lives in another database. */
    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Embedded
    private VehicleSnapshot vehicle;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 30)
    private ServiceType serviceType;

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "ends_at", nullable = false)
    private LocalDateTime endsAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status = BookingStatus.BOOKED;

    @Column(length = 500)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Booking() {
        // required by JPA
    }

    public Booking(Long vehicleId, VehicleSnapshot vehicle, ServiceType serviceType,
                   LocalDateTime startsAt, LocalDateTime endsAt, String notes) {
        this.vehicleId = vehicleId;
        this.vehicle = vehicle;
        this.serviceType = serviceType;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.notes = notes;
    }

    public void start() {
        moveTo(BookingStatus.IN_PROGRESS);
    }

    public void complete() {
        moveTo(BookingStatus.COMPLETED);
    }

    public void cancel() {
        moveTo(BookingStatus.CANCELLED);
    }

    private void moveTo(BookingStatus next) {
        if (!status.canMoveTo(next)) {
            throw new BusinessRuleException(
                    "Booking %d cannot move from %s to %s".formatted(id, status, next));
        }
        status = next;
    }

    public Long getId() {
        return id;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public VehicleSnapshot getVehicle() {
        return vehicle;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public LocalDateTime getEndsAt() {
        return endsAt;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Booking other)) {
            return false;
        }
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
