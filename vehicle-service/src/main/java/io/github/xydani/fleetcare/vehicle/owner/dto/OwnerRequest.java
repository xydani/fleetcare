package io.github.xydani.fleetcare.vehicle.owner.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OwnerRequest(
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @NotBlank @Email @Size(max = 160) String email,
        @Pattern(regexp = "^\\+?[0-9 ]{6,30}$", message = "must be a phone number made of digits and spaces")
        String phone
) {
}
