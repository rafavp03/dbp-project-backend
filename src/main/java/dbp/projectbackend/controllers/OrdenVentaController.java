package dbp.projectbackend.controllers;

import dbp.projectbackend.dtos.OrdenVentaDTO;
import dbp.projectbackend.dtos.OrdenVentaResponseDTO;
import dbp.projectbackend.services.OrdenVentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/orden-venta")

public class OrdenVentaController {
    private final OrdenVentaService ordenVentaService;

    @PostMapping
    public ResponseEntity<OrdenVentaResponseDTO> createOrdenVenta(@Valid @RequestBody OrdenVentaDTO ordenVentaDTO) {
        OrdenVentaResponseDTO nuevaOrden = ordenVentaService.createOrdenVenta(ordenVentaDTO);
        return ResponseEntity
                .created(URI.create("/orden-venta/" + nuevaOrden.id()))
                .body(nuevaOrden);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenVentaResponseDTO> getOrdenVentaById(@PathVariable Long id) {
        return ResponseEntity.ok(ordenVentaService.getOrdenVentaById(id));
    }
}
