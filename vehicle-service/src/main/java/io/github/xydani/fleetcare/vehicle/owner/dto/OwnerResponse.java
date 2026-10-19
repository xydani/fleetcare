package io.github.xydani.fleetcare.vehicle.owner.dto;

import java.time.Instant;

public record OwnerResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        Instant createdAt
) {
}
