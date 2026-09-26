package dbp.projectbackend.controllers;

import dbp.projectbackend.dtos.request.SalesOrderDTO;
import dbp.projectbackend.dtos.response.SalesOrderResponseDTO;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.services.SalesOrderService;
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
@RequestMapping("/api/v1/sales-orders")

public class SalesOrderController {
    private final SalesOrderService ordenVentaService;

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @PostMapping
    public ResponseEntity<SalesOrderResponseDTO> createSalesOrder(@AuthenticationPrincipal UserModel user, @Valid @RequestBody SalesOrderDTO ordenVentaDTO) {
        SalesOrderResponseDTO nuevaOrden = ordenVentaService.createSalesOrder(user, ordenVentaDTO);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(nuevaOrden.id())
                .toUri();
        return ResponseEntity.created(location).body(nuevaOrden);
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @GetMapping("/{id}")
    public ResponseEntity<SalesOrderResponseDTO> getSalesOrderById(@AuthenticationPrincipal UserModel user, @PathVariable Long id) {
        return ResponseEntity.ok(ordenVentaService.getSalesOrderById(user, id));
    }
}
