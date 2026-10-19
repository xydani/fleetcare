package io.github.xydani.fleetcare.maintenance.intervention;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import io.github.xydani.fleetcare.maintenance.booking.Booking;
import io.github.xydani.fleetcare.maintenance.pricing.CostBreakdown;
import io.github.xydani.fleetcare.maintenance.pricing.PricingPlan;

/**
 * The work actually done for a booking, with what it cost. Created when the booking is completed.
 */
@Entity
@Table(name = "intervention")
public class Intervention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_plan", nullable = false, length = 20)
    private PricingPlan pricingPlan;

    @Column(name = "labour_hours", nullable = false)
    private BigDecimal labourHours;

    @Embedded
    private CostBreakdown cost;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Intervention() {
        // required by JPA
    }

    public Intervention(Booking booking, String description, PricingPlan pricingPlan, BigDecimal labourHours,
                        CostBreakdown cost, LocalDateTime completedAt) {
        this.booking = booking;
        this.description = description;
        this.pricingPlan = pricingPlan;
        this.labourHours = labourHours;
        this.cost = cost;
        this.completedAt = completedAt;
    }

    public Long getId() {
        return id;
    }

    public Booking getBooking() {
        return booking;
    }

    public String getDescription() {
        return description;
    }

    public PricingPlan getPricingPlan() {
        return pricingPlan;
    }

    public BigDecimal getLabourHours() {
        return labourHours;
    }

    public CostBreakdown getCost() {
        return cost;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Intervention other)) {
            return false;
        }
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
