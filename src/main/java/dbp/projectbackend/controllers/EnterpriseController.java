package dbp.projectbackend.controllers;

import dbp.projectbackend.dtos.EnterpriseDTO;
import dbp.projectbackend.models.EnterpriseModel;
import dbp.projectbackend.services.EnterpriseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/enterprise")
public class EnterpriseController {
    private final EnterpriseService service;

    @PostMapping
    public ResponseEntity<EnterpriseModel> createEnterprise(@Valid @RequestBody EnterpriseDTO dto) {
        EnterpriseModel newEnterprise = service.createEnterprise(dto);
        return ResponseEntity
                .created(URI.create("enterprise/"+newEnterprise.getId()))
                .body(newEnterprise);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnterpriseDTO> getEnterpriseById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getEnterpriseById(id));
    }
}
