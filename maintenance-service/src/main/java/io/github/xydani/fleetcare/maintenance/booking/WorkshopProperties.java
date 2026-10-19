package io.github.xydani.fleetcare.maintenance.booking;

import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Workshop capacity and opening hours, from {@code fleetcare.workshop.*}.
 *
 * @param bays     vehicles that can be worked on at the same time
 * @param opensAt  earliest start of a booking, Monday to Friday
 * @param closesAt latest end of a booking
 */
@Validated
@ConfigurationProperties("fleetcare.workshop")
public record WorkshopProperties(
        @Positive int bays,
        @NotNull LocalTime opensAt,
        @NotNull LocalTime closesAt
) {
}
