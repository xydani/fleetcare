package io.github.xydani.fleetcare.vehicle.vehicle;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import io.github.xydani.fleetcare.vehicle.owner.Owner;
import io.github.xydani.fleetcare.vehicle.vehicle.dto.OwnerSummary;
import io.github.xydani.fleetcare.vehicle.vehicle.dto.VehicleRequest;
import io.github.xydani.fleetcare.vehicle.vehicle.dto.VehicleResponse;

@Mapper
public interface VehicleMapper {

    VehicleResponse toResponse(Vehicle vehicle);

    @Mapping(target = "fullName", expression = "java(owner.getFirstName() + \" \" + owner.getLastName())")
    OwnerSummary toOwnerSummary(Owner owner);

    /**
     * Copies the editable fields of the request onto an existing vehicle.
     * The owner is resolved by the service from {@code ownerId}.
     */
    @Mapping(target = "owner", ignore = true)
    void update(@MappingTarget Vehicle vehicle, VehicleRequest request);
}
