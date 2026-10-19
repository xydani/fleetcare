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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import io.github.xydani.fleetcare.vehicle.common.web.PageResponse;
import io.github.xydani.fleetcare.vehicle.owner.dto.OwnerRequest;
import io.github.xydani.fleetcare.vehicle.owner.dto.OwnerResponse;

@RestController
@RequestMapping("/api/owners")
@Tag(name = "Owners", description = "People and companies that own vehicles")
public class OwnerController {

    private final OwnerService ownerService;

    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    @Operation(summary = "List owners", description = "Paginated, sorted by last name by default.")
    @GetMapping
    public PageResponse<OwnerResponse> findAll(
            @PageableDefault(size = 20, sort = "lastName", direction = Sort.Direction.ASC) Pageable pageable) {
        return ownerService.findAll(pageable);
    }

    @Operation(summary = "Get an owner")
    @GetMapping("/{id}")
    public OwnerResponse findById(@PathVariable Long id) {
        return ownerService.findById(id);
    }

    @Operation(summary = "Create an owner", description = "Emails are unique, case-insensitive (409 on duplicates).")
    @PostMapping
    public ResponseEntity<OwnerResponse> create(@Valid @RequestBody OwnerRequest request) {
        OwnerResponse created = ownerService.create(request);
        return ResponseEntity.created(URI.create("/api/owners/" + created.id())).body(created);
    }

    @Operation(summary = "Update an owner")
    @PutMapping("/{id}")
    public OwnerResponse update(@PathVariable Long id, @Valid @RequestBody OwnerRequest request) {
        return ownerService.update(id, request);
    }

    @Operation(summary = "Delete an owner", description = "Refused with 409 while the owner has vehicles.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ownerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
