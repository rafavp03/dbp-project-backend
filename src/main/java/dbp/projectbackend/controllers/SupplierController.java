package dbp.projectbackend.controllers;

import dbp.projectbackend.dtos.request.SupplierDTO;
import dbp.projectbackend.dtos.response.SupplierResponseDTO;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.services.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/suppliers")
public class SupplierController {

    private final SupplierService service;

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @PostMapping
    public ResponseEntity<SupplierResponseDTO> createSupplier(@AuthenticationPrincipal UserModel user, @Valid @RequestBody SupplierDTO dto) {
        SupplierResponseDTO newSupplier = service.createSupplier(user, dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newSupplier.id())
                .toUri();
        return ResponseEntity.created(location).body(newSupplier);
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponseDTO> getSupplier(@AuthenticationPrincipal UserModel user, @PathVariable Long id) {
        return ResponseEntity.ok(service.getSupplierById(user, id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @GetMapping
    public ResponseEntity<List<SupplierResponseDTO>> getSuppliers(@AuthenticationPrincipal UserModel user) {
        return ResponseEntity.ok(service.getSuppliersByEnterprise(user));
    }
}
