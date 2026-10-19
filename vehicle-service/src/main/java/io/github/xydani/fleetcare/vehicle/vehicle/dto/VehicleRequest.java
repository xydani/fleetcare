package io.github.xydani.fleetcare.vehicle.vehicle.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import io.github.xydani.fleetcare.vehicle.vehicle.FuelType;

public record VehicleRequest(
        /* Italian plate since 1994: AA 000 AA, letters I, O, Q and U are never used. */
        @NotBlank
        @Pattern(regexp = "^[A-HJ-NPR-TV-Z]{2}[0-9]{3}[A-HJ-NPR-TV-Z]{2}$",
                message = "must be an Italian plate such as AB123CD")
        String plate,
        @NotBlank @Size(max = 60) String brand,
        @NotBlank @Size(max = 60) String model,
        @NotNull @Min(1950) @Max(2100) Integer productionYear,
        @NotNull FuelType fuelType,
        @NotNull @PastOrPresent LocalDate registrationDate,
        @NotNull Long ownerId
) {
}
