package dbp.projectbackend.controllers;

import dbp.projectbackend.dtos.OrdenVentaDTO;
import dbp.projectbackend.models.OrdenVentaModel;
import dbp.projectbackend.services.OrdenVentaService;
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
    public ResponseEntity<OrdenVentaModel> createOrdenVenta(@RequestBody OrdenVentaDTO ordenVentaDTO) {
        OrdenVentaModel nuevaOrden = ordenVentaService.createOrdenVenta(ordenVentaDTO);
        return ResponseEntity
                .created(URI.create("/orden-venta/" + nuevaOrden.getId()))
                .body(nuevaOrden);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenVentaModel> getOrdenVentaById(@PathVariable Long id) {
        return ResponseEntity.ok(ordenVentaService.getOrdenVentaById(id));
    }
}
