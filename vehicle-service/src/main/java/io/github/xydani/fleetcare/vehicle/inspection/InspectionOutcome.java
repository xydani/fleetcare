package io.github.xydani.fleetcare.vehicle.inspection;

import java.time.LocalDate;

/**
 * Outcomes of an Italian periodic inspection ("revisione"), each with its own validity.
 */
public enum InspectionOutcome {

    /** Vehicle is roadworthy ("regolare"): valid for two years. */
    PASSED {
        @Override
        public LocalDate expiryFrom(LocalDate inspectionDate) {
            return inspectionDate.plusYears(2);
        }
    },

    /** Defects found ("ripetere"): must be inspected again within a month. */
    REPEAT {
        @Override
        public LocalDate expiryFrom(LocalDate inspectionDate) {
            return inspectionDate.plusMonths(1);
        }
    },

    /** Vehicle must not be driven until repaired ("sospeso"): no validity. */
    SUSPENDED {
        @Override
        public LocalDate expiryFrom(LocalDate inspectionDate) {
            return inspectionDate.plusDays(1);
        }
    };

    /**
     * Date from which the vehicle needs a new inspection.
     */
    public abstract LocalDate expiryFrom(LocalDate inspectionDate);
}
