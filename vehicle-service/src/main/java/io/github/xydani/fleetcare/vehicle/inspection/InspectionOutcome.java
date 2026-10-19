package io.github.xydani.fleetcare.vehicle.inspection;

/**
 * Outcomes of an Italian periodic inspection ("revisione").
 */
public enum InspectionOutcome {
    /** Vehicle is roadworthy ("regolare"). */
    PASSED,
    /** Defects found, must be inspected again ("ripetere"). */
    REPEAT,
    /** Vehicle must not be driven until repaired ("sospeso"). */
    SUSPENDED
}
