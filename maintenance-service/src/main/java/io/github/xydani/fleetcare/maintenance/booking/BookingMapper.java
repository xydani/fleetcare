package io.github.xydani.fleetcare.maintenance.booking;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import io.github.xydani.fleetcare.maintenance.booking.dto.BookingResponse;

@Mapper
public interface BookingMapper {

    @Mapping(target = "plate", source = "vehicle.plate")
    @Mapping(target = "brand", source = "vehicle.brand")
    @Mapping(target = "model", source = "vehicle.model")
    BookingResponse toResponse(Booking booking);
}
