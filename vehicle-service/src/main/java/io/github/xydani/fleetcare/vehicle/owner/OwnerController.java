package io.github.xydani.fleetcare.vehicle.owner;

import java.net.URI;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.xydani.fleetcare.vehicle.common.web.PageResponse;
import io.github.xydani.fleetcare.vehicle.owner.dto.OwnerRequest;
import io.github.xydani.fleetcare.vehicle.owner.dto.OwnerResponse;

@RestController
@RequestMapping("/api/owners")
public class OwnerController {

    private final OwnerService ownerService;

    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    @GetMapping
    public PageResponse<OwnerResponse> findAll(
            @PageableDefault(size = 20, sort = "lastName", direction = Sort.Direction.ASC) Pageable pageable) {
        return ownerService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public OwnerResponse findById(@PathVariable Long id) {
        return ownerService.findById(id);
    }

    @PostMapping
    public ResponseEntity<OwnerResponse> create(@Valid @RequestBody OwnerRequest request) {
        OwnerResponse created = ownerService.create(request);
        return ResponseEntity.created(URI.create("/api/owners/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public OwnerResponse update(@PathVariable Long id, @Valid @RequestBody OwnerRequest request) {
        return ownerService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ownerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
