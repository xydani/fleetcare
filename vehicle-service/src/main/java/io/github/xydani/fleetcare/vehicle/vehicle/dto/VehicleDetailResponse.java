package io.github.xydani.fleetcare.vehicle.vehicle.dto;

import java.time.LocalDate;
import java.util.Objects;

import io.github.xydani.fleetcare.vehicle.inspection.InspectionOutcome;
import io.github.xydani.fleetcare.vehicle.vehicle.FuelType;
import io.github.xydani.fleetcare.vehicle.vehicle.InspectionStatus;

/**
 * Full view of a vehicle, combining its registry data with the latest mileage and inspection.
 * Mileage and last-inspection fields are null when nothing has been recorded yet.
 * Built through {@link #builder()} because many fields are optional.
 */
public record VehicleDetailResponse(
        Long id,
        String plate,
        String brand,
        String model,
        Integer productionYear,
        FuelType fuelType,
        LocalDate registrationDate,
        OwnerSummary owner,
        Integer currentKm,
        LocalDate lastReadingDate,
        LocalDate lastInspectionDate,
        InspectionOutcome lastInspectionOutcome,
        LocalDate nextInspectionDue,
        InspectionStatus inspectionStatus
) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Long id;
        private String plate;
        private String brand;
        private String model;
        private Integer productionYear;
        private FuelType fuelType;
        private LocalDate registrationDate;
        private OwnerSummary owner;
        private Integer currentKm;
        private LocalDate lastReadingDate;
        private LocalDate lastInspectionDate;
        private InspectionOutcome lastInspectionOutcome;
        private LocalDate nextInspectionDue;
        private InspectionStatus inspectionStatus;

        private Builder() {
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder plate(String plate) {
            this.plate = plate;
            return this;
        }

        public Builder brand(String brand) {
            this.brand = brand;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder productionYear(Integer productionYear) {
            this.productionYear = productionYear;
            return this;
        }

        public Builder fuelType(FuelType fuelType) {
            this.fuelType = fuelType;
            return this;
        }

        public Builder registrationDate(LocalDate registrationDate) {
            this.registrationDate = registrationDate;
            return this;
        }

        public Builder owner(OwnerSummary owner) {
            this.owner = owner;
            return this;
        }

        /** Sets the latest odometer reading and its date. */
        public Builder mileage(Integer currentKm, LocalDate lastReadingDate) {
            this.currentKm = currentKm;
            this.lastReadingDate = lastReadingDate;
            return this;
        }

        /** Sets the date and outcome of the latest inspection. */
        public Builder lastInspection(LocalDate date, InspectionOutcome outcome) {
            this.lastInspectionDate = date;
            this.lastInspectionOutcome = outcome;
            return this;
        }

        /** Sets the next due date together with its status relative to today. */
        public Builder nextInspection(LocalDate dueDate, InspectionStatus status) {
            this.nextInspectionDue = dueDate;
            this.inspectionStatus = status;
            return this;
        }

        public VehicleDetailResponse build() {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(plate, "plate");
            Objects.requireNonNull(owner, "owner");
            Objects.requireNonNull(nextInspectionDue, "nextInspectionDue");
            return new VehicleDetailResponse(id, plate, brand, model, productionYear, fuelType, registrationDate,
                    owner, currentKm, lastReadingDate, lastInspectionDate, lastInspectionOutcome,
                    nextInspectionDue, inspectionStatus);
        }
    }
}
