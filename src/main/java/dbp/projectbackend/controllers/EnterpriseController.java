package dbp.projectbackend.controllers;

import dbp.projectbackend.dtos.request.EnterpriseDTO;
import dbp.projectbackend.dtos.response.EnterpriseResponseDTO;
import dbp.projectbackend.services.EnterpriseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/enterprise")
public class EnterpriseController {
    private final EnterpriseService service;

    @PostMapping
    public ResponseEntity<EnterpriseResponseDTO> createEnterprise(@Valid @RequestBody EnterpriseDTO dto) {
        EnterpriseResponseDTO newEnterprise = service.createEnterprise(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newEnterprise.id())
                .toUri();
        return ResponseEntity.created(location).body(newEnterprise);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnterpriseResponseDTO> getEnterpriseById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getEnterpriseById(id));
    }
}
