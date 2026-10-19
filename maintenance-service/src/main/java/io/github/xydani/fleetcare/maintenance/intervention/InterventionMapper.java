package io.github.xydani.fleetcare.maintenance.intervention;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import io.github.xydani.fleetcare.maintenance.intervention.dto.InterventionResponse;

@Mapper
public interface InterventionMapper {

    @Mapping(target = "bookingId", source = "booking.id")
    @Mapping(target = "vehicleId", source = "booking.vehicleId")
    @Mapping(target = "plate", source = "booking.vehicle.plate")
    @Mapping(target = "brand", source = "booking.vehicle.brand")
    @Mapping(target = "model", source = "booking.vehicle.model")
    @Mapping(target = "serviceType", source = "booking.serviceType")
    @Mapping(target = "partsCost", source = "cost.partsCost")
    @Mapping(target = "labourCost", source = "cost.labourCost")
    @Mapping(target = "discount", source = "cost.discount")
    @Mapping(target = "totalCost", source = "cost.totalCost")
    InterventionResponse toResponse(Intervention intervention);
}
