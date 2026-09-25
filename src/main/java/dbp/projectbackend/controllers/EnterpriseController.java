package dbp.projectbackend.controllers;

import dbp.projectbackend.dtos.EnterpriseDTO;
import dbp.projectbackend.dtos.EnterpriseResponseDTO;
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

    // Publico: un negocio nuevo se registra sin cuenta todavia (ver SecurityConfig)
    @PostMapping
    public ResponseEntity<EnterpriseResponseDTO> createEnterprise(@Valid @RequestBody EnterpriseDTO dto) {
        EnterpriseResponseDTO newEnterprise = service.createEnterprise(dto);
        return ResponseEntity
                .created(URI.create("/enterprise/" + newEnterprise.id()))
                .body(newEnterprise);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnterpriseResponseDTO> getEnterpriseById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getEnterpriseById(id));
    }
}
