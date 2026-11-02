package dbp.projectbackend.controllers;

import dbp.projectbackend.dtos.request.PurchaseOrderDTO;
import dbp.projectbackend.dtos.response.PageResponseDTO;
import dbp.projectbackend.dtos.response.PurchaseOrderResponseDTO;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.services.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/purchase-orders")

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
    @GetMapping
    public ResponseEntity<PageResponseDTO<PurchaseOrderResponseDTO>> getPurchaseOrders(
            @AuthenticationPrincipal UserModel user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @PageableDefault(size = 20, sort = "fechaEmision", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ordenCompraService.getPurchaseOrders(user, desde, hasta, pageable));
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponseDTO> getPurchaseOrderById(@AuthenticationPrincipal UserModel user, @PathVariable Long id) {
        return ResponseEntity.ok(ordenCompraService.getPurchaseOrderById(user, id));
    }
}
