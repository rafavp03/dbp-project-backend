package dbp.projectbackend.controllers;

import dbp.projectbackend.dtos.SupplierDTO;
import dbp.projectbackend.dtos.SupplierResponseDTO;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.services.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/supplier")
public class SupplierController {

    private final SupplierService service;

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @PostMapping
    public ResponseEntity<SupplierResponseDTO> createSupplier(@AuthenticationPrincipal UserModel user, @Valid @RequestBody SupplierDTO dto) {
        SupplierResponseDTO newSupplier = service.createSupplier(user, dto);
        return ResponseEntity
                .created(URI.create("/supplier/" + newSupplier.id()))
                .body(newSupplier);
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