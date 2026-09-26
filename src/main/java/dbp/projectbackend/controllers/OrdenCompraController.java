package dbp.projectbackend.controllers;

import dbp.projectbackend.dtos.OrdenCompraDTO;
import dbp.projectbackend.dtos.OrdenCompraResponseDTO;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.services.OrdenCompraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/orden-compra")

public class OrdenCompraController {
    private final OrdenCompraService ordenCompraService;

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @PostMapping
    public ResponseEntity<OrdenCompraResponseDTO> createOrdenCompra(@AuthenticationPrincipal UserModel user, @Valid @RequestBody OrdenCompraDTO ordenCompraDTO) {
        OrdenCompraResponseDTO nuevaOrden = ordenCompraService.createOrdenCompra(user, ordenCompraDTO);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(nuevaOrden.id())
                .toUri();
        return ResponseEntity.created(location).body(nuevaOrden);
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @GetMapping("/{id}")
    public ResponseEntity<OrdenCompraResponseDTO> getOrdenCompraById(@AuthenticationPrincipal UserModel user, @PathVariable Long id) {
        return ResponseEntity.ok(ordenCompraService.getOrdenCompraById(user, id));
    }
}