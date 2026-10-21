package io.github.xydani.fleetcare.vehicle.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.xydani.fleetcare.vehicle.common.error.ConflictException;
import io.github.xydani.fleetcare.vehicle.common.web.PageResponse;
import io.github.xydani.fleetcare.vehicle.owner.dto.OwnerRequest;
import io.github.xydani.fleetcare.vehicle.owner.dto.OwnerResponse;
import io.github.xydani.fleetcare.vehicle.vehicle.VehicleRepository;

@Service
@Transactional(readOnly = true)
public class OwnerService {

    private static final Logger log = LoggerFactory.getLogger(OwnerService.class);

    private final OwnerRepository ownerRepository;
    private final VehicleRepository vehicleRepository;
    private final OwnerMapper ownerMapper;

    public OwnerService(OwnerRepository ownerRepository, VehicleRepository vehicleRepository,
                        OwnerMapper ownerMapper) {
        this.ownerRepository = ownerRepository;
        this.vehicleRepository = vehicleRepository;
        this.ownerMapper = ownerMapper;
    }

    public PageResponse<OwnerResponse> findAll(Pageable pageable) {
        return PageResponse.of(ownerRepository.findAll(pageable), ownerMapper::toResponse);
    }

    public OwnerResponse findById(Long id) {
        return ownerMapper.toResponse(getOwner(id));
    }

    @Transactional
    public OwnerResponse create(OwnerRequest request) {
        if (ownerRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateEmailException(request.email());
        }
        Owner owner = new Owner(request.firstName(), request.lastName(), request.email(), request.phone());
        owner = ownerRepository.save(owner);
        log.info("Owner {} registered", owner.getId());
        return ownerMapper.toResponse(owner);
    }

    @Transactional
    public OwnerResponse update(Long id, OwnerRequest request) {
        Owner owner = getOwner(id);
        if (ownerRepository.existsByEmailIgnoreCaseAndIdNot(request.email(), id)) {
            throw new DuplicateEmailException(request.email());
        }
        ownerMapper.update(owner, request);
        return ownerMapper.toResponse(owner);
    }

    @Transactional
    public void delete(Long id) {
        Owner owner = getOwner(id);
        if (vehicleRepository.existsByOwnerId(id)) {
            throw new ConflictException("Owner " + id + " still has registered vehicles");
        }
        ownerRepository.delete(owner);
        log.info("Owner {} deleted", id);
    }

    Owner getOwner(Long id) {
        return ownerRepository.findById(id).orElseThrow(() -> new OwnerNotFoundException(id));
    }
}
