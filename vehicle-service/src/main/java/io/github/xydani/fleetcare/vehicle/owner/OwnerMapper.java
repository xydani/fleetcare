package io.github.xydani.fleetcare.vehicle.owner;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import io.github.xydani.fleetcare.vehicle.owner.dto.OwnerRequest;
import io.github.xydani.fleetcare.vehicle.owner.dto.OwnerResponse;

@Mapper
public interface OwnerMapper {

    OwnerResponse toResponse(Owner owner);

    /**
     * Copies the editable fields of the request onto an existing owner.
     */
    void update(@MappingTarget Owner owner, OwnerRequest request);
}
