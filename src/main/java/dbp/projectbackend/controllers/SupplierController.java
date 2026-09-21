package dbp.projectbackend.controllers;

import dbp.projectbackend.dtos.SupplierDTO;
import dbp.projectbackend.models.SupplierModel;
import dbp.projectbackend.services.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/supplier")
public class SupplierController {

    private final SupplierService service;

    @PostMapping
    public ResponseEntity<SupplierModel> createSupplier(@RequestParam Long enterpriseId, @Valid @RequestBody SupplierDTO dto) {
        SupplierModel newSupplier = service.createSupplier(enterpriseId, dto);
        return ResponseEntity
                .created(URI.create("supplier/"+newSupplier.getId()))
                .body(newSupplier);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierDTO> getSupplier(@PathVariable Long id) {
        return ResponseEntity.ok(service.getSupplierById(id));
    }
}
