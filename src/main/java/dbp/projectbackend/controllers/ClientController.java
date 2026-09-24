package dbp.projectbackend.controllers;

import dbp.projectbackend.dtos.ClientDTO;
import dbp.projectbackend.dtos.ClientResponseDTO;
import dbp.projectbackend.services.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/client")
public class ClientController {

    private final ClientService service;

    @PostMapping
    public ResponseEntity<ClientResponseDTO> createClient(@RequestParam Long enterpriseId, @Valid @RequestBody ClientDTO dto) {
        ClientResponseDTO newClient = service.createClient(enterpriseId, dto);
        return ResponseEntity
                .created(URI.create("client/"+newClient.id()))
                .body(newClient);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> getClient(@PathVariable Long id) {
        return ResponseEntity.ok(service.getClientById(id));
    }

    @GetMapping
    public ResponseEntity<List<ClientResponseDTO>> getClientsByEnterprise(@RequestParam Long enterpriseId) {
        return ResponseEntity.ok(service.getClientsByEnterprise(enterpriseId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> updateClient(@PathVariable Long id, @Valid @RequestBody ClientDTO dto) {
        return ResponseEntity.ok(service.updateClient(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        service.deleteClient(id);
        return ResponseEntity.noContent().build();
    }
}
