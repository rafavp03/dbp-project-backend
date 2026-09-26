package dbp.projectbackend.controllers;

import dbp.projectbackend.dtos.request.PurchaseOrderDTO;
import dbp.projectbackend.dtos.response.PurchaseOrderResponseDTO;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.services.PurchaseOrderService;
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

public class PurchaseOrderController {
    private final PurchaseOrderService ordenCompraService;

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @PostMapping
    public ResponseEntity<PurchaseOrderResponseDTO> createPurchaseOrder(@AuthenticationPrincipal UserModel user, @Valid @RequestBody PurchaseOrderDTO ordenCompraDTO) {
        PurchaseOrderResponseDTO nuevaOrden = ordenCompraService.createPurchaseOrder(user, ordenCompraDTO);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(nuevaOrden.id())
                .toUri();
        return ResponseEntity.created(location).body(nuevaOrden);
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponseDTO> getPurchaseOrderById(@AuthenticationPrincipal UserModel user, @PathVariable Long id) {
        return ResponseEntity.ok(ordenCompraService.getPurchaseOrderById(user, id));
    }
}
