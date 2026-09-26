package dbp.projectbackend.controllers;

import dbp.projectbackend.dtos.ClientDTO;
import dbp.projectbackend.dtos.ClientResponseDTO;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.services.ClientService;
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
@RequestMapping("/client")
public class ClientController {

    private final ClientService service;

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @PostMapping
    public ResponseEntity<ClientResponseDTO> createClient(@AuthenticationPrincipal UserModel user, @Valid @RequestBody ClientDTO dto) {
        ClientResponseDTO newClient = service.createClient(user, dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newClient.id())
                .toUri();
        return ResponseEntity.created(location).body(newClient);
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> getClient(@AuthenticationPrincipal UserModel user, @PathVariable Long id) {
        return ResponseEntity.ok(service.getClientById(user, id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @GetMapping
    public ResponseEntity<List<ClientResponseDTO>> getClients(@AuthenticationPrincipal UserModel user) {
        return ResponseEntity.ok(service.getClientsByEnterprise(user));
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @PutMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> updateClient(@AuthenticationPrincipal UserModel user, @PathVariable Long id, @Valid @RequestBody ClientDTO dto) {
        return ResponseEntity.ok(service.updateClient(user, id, dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@AuthenticationPrincipal UserModel user, @PathVariable Long id) {
        service.deleteClient(user, id);
        return ResponseEntity.noContent().build();
    }
}