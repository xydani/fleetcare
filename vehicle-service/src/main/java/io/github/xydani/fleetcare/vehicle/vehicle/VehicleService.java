package io.github.xydani.fleetcare.vehicle.vehicle;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.xydani.fleetcare.vehicle.common.error.BusinessRuleException;
import io.github.xydani.fleetcare.vehicle.common.web.PageResponse;
import io.github.xydani.fleetcare.vehicle.owner.Owner;
import io.github.xydani.fleetcare.vehicle.owner.OwnerRepository;
import io.github.xydani.fleetcare.vehicle.vehicle.dto.VehicleRequest;
import io.github.xydani.fleetcare.vehicle.vehicle.dto.VehicleResponse;

@Service
@Transactional(readOnly = true)
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final OwnerRepository ownerRepository;
    private final VehicleMapper vehicleMapper;

    public VehicleService(VehicleRepository vehicleRepository, OwnerRepository ownerRepository,
                          VehicleMapper vehicleMapper) {
        this.vehicleRepository = vehicleRepository;
        this.ownerRepository = ownerRepository;
        this.vehicleMapper = vehicleMapper;
    }

    public PageResponse<VehicleResponse> findAll(String brand, FuelType fuelType, Long ownerId, Pageable pageable) {
        var spec = VehicleSpecifications.withFilters(brand, fuelType, ownerId);
        return PageResponse.of(vehicleRepository.findAll(spec, pageable), vehicleMapper::toResponse);
    }

    public VehicleResponse findById(Long id) {
        return vehicleMapper.toResponse(getVehicle(id));
    }

    @Transactional
    public VehicleResponse create(VehicleRequest request) {
        if (vehicleRepository.existsByPlate(request.plate())) {
            throw new DuplicatePlateException(request.plate());
        }
        checkRegistrationAfterProduction(request);
        Owner owner = getOwner(request.ownerId());

        Vehicle vehicle = new Vehicle(request.plate(), request.brand(), request.model(), request.productionYear(),
                request.fuelType(), request.registrationDate(), owner);
        return vehicleMapper.toResponse(vehicleRepository.save(vehicle));
    }

    @Transactional
    public VehicleResponse update(Long id, VehicleRequest request) {
        Vehicle vehicle = getVehicle(id);
        if (vehicleRepository.existsByPlateAndIdNot(request.plate(), id)) {
            throw new DuplicatePlateException(request.plate());
        }
        checkRegistrationAfterProduction(request);

        vehicleMapper.update(vehicle, request);
        vehicle.setOwner(getOwner(request.ownerId()));
        return vehicleMapper.toResponse(vehicle);
    }

    /**
     * Deletes the vehicle; its mileage readings and inspections are removed by the database cascade.
     */
    @Transactional
    public void delete(Long id) {
        vehicleRepository.delete(getVehicle(id));
    }

    public Vehicle getVehicle(Long id) {
        return vehicleRepository.findWithOwnerById(id).orElseThrow(() -> new VehicleNotFoundException(id));
    }

    private Owner getOwner(Long ownerId) {
        return ownerRepository.findById(ownerId)
                .orElseThrow(() -> new BusinessRuleException("Owner " + ownerId + " does not exist"));
    }

    private static void checkRegistrationAfterProduction(VehicleRequest request) {
        if (request.registrationDate().getYear() < request.productionYear()) {
            throw new BusinessRuleException("Registration date cannot be before the production year");
        }
    }
}
