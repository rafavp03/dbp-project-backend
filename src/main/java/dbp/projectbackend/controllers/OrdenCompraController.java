package dbp.projectbackend.controllers;

import dbp.projectbackend.dtos.OrdenCompraDTO;
import dbp.projectbackend.dtos.OrdenCompraResponseDTO;
import dbp.projectbackend.services.OrdenCompraService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/orden-compra")

public class OrdenCompraController {
    private final OrdenCompraService ordenCompraService;

    @PostMapping
    public ResponseEntity<OrdenCompraResponseDTO> createOrdenCompra(@RequestBody OrdenCompraDTO ordenCompraDTO) {
        OrdenCompraResponseDTO nuevaOrden = ordenCompraService.createOrdenCompra(ordenCompraDTO);
        return ResponseEntity
                .created(URI.create("/orden-compra/" + nuevaOrden.id()))
                .body(nuevaOrden);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenCompraResponseDTO> getOrdenCompraById(@PathVariable Long id) {
        return ResponseEntity.ok(ordenCompraService.getOrdenCompraById(id));
    }
}
