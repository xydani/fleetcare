package io.github.xydani.fleetcare.vehicle.inspection;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import io.github.xydani.fleetcare.vehicle.inspection.dto.InspectionResponse;

@Mapper
public interface InspectionMapper {

    @Mapping(target = "vehicleId", source = "vehicle.id")
    InspectionResponse toResponse(Inspection inspection);
}
