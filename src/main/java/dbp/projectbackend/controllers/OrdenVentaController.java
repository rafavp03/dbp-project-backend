package dbp.projectbackend.controllers;

import dbp.projectbackend.dtos.OrdenVentaDTO;
import dbp.projectbackend.dtos.OrdenVentaResponseDTO;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.services.OrdenVentaService;
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
@RequestMapping("/orden-venta")

public class OrdenVentaController {
    private final OrdenVentaService ordenVentaService;

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @PostMapping
    public ResponseEntity<OrdenVentaResponseDTO> createOrdenVenta(@AuthenticationPrincipal UserModel user, @Valid @RequestBody OrdenVentaDTO ordenVentaDTO) {
        OrdenVentaResponseDTO nuevaOrden = ordenVentaService.createOrdenVenta(user, ordenVentaDTO);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(nuevaOrden.id())
                .toUri();
        return ResponseEntity.created(location).body(nuevaOrden);
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @GetMapping("/{id}")
    public ResponseEntity<OrdenVentaResponseDTO> getOrdenVentaById(@AuthenticationPrincipal UserModel user, @PathVariable Long id) {
        return ResponseEntity.ok(ordenVentaService.getOrdenVentaById(user, id));
    }
}