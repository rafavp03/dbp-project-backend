package dbp.projectbackend.controllers;

import dbp.projectbackend.dtos.request.StockAdjustmentDTO;
import dbp.projectbackend.dtos.request.StockMovementDTO;
import dbp.projectbackend.dtos.response.StockMovementResponseDTO;
import dbp.projectbackend.dtos.response.ProductVariantResponseDTO;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.services.StockMovementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/stock")
public class StockMovementController {

    private final StockMovementService service;

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @PostMapping("/entrada")
    public ResponseEntity<StockMovementResponseDTO> registerInbound(@AuthenticationPrincipal UserModel user, @Valid @RequestBody StockMovementDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registerInbound(user, dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @PostMapping("/salida")
    public ResponseEntity<StockMovementResponseDTO> registerOutbound(@AuthenticationPrincipal UserModel user, @Valid @RequestBody StockMovementDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registerOutbound(user, dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @PostMapping("/devolucion")
    public ResponseEntity<StockMovementResponseDTO> registerReturn(@AuthenticationPrincipal UserModel user, @Valid @RequestBody StockMovementDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registerReturn(user, dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @PostMapping("/ajuste")
    public ResponseEntity<StockMovementResponseDTO> registerAdjustment(@AuthenticationPrincipal UserModel user, @Valid @RequestBody StockAdjustmentDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registerAdjustment(user, dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @GetMapping("/variante/{varianteId}")
    public ResponseEntity<List<StockMovementResponseDTO>> getKardex(@AuthenticationPrincipal UserModel user, @PathVariable Long varianteId) {
        return ResponseEntity.ok(service.getKardexByVariant(user, varianteId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @GetMapping("/bajo")
    public ResponseEntity<List<ProductVariantResponseDTO>> getLowStock(@AuthenticationPrincipal UserModel user) {
        return ResponseEntity.ok(service.getLowStock(user));
    }
}
