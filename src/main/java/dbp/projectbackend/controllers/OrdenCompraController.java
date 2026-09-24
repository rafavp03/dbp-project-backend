package dbp.projectbackend.controllers;

import dbp.projectbackend.dtos.OrdenCompraDTO;
import dbp.projectbackend.models.OrdenCompraModel;
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
    public ResponseEntity<OrdenCompraModel> createOrdenCompra(@RequestBody OrdenCompraDTO ordenCompraDTO) {
        OrdenCompraModel nuevaOrden = ordenCompraService.createOrdenCompra(ordenCompraDTO);
        return ResponseEntity
                .created(URI.create("/orden-compra/" + nuevaOrden.getId()))
                .body(nuevaOrden);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenCompraModel> getOrdenCompraById(@PathVariable Long id) {
        return ResponseEntity.ok(ordenCompraService.getOrdenCompraById(id));
    }
}
