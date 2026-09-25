package dbp.projectbackend.controllers;

import dbp.projectbackend.dtos.AjusteStockDTO;
import dbp.projectbackend.dtos.MovimientoStockDTO;
import dbp.projectbackend.dtos.MovimientoStockResponseDTO;
import dbp.projectbackend.dtos.VarianteProductoResponseDTO;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.services.MovimientoStockService;
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
public class MovimientoStockController {

    private final MovimientoStockService service;

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @PostMapping("/entrada")
    public ResponseEntity<MovimientoStockResponseDTO> registrarEntrada(@AuthenticationPrincipal UserModel user, @Valid @RequestBody MovimientoStockDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarEntrada(user, dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @PostMapping("/salida")
    public ResponseEntity<MovimientoStockResponseDTO> registrarSalida(@AuthenticationPrincipal UserModel user, @Valid @RequestBody MovimientoStockDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarSalida(user, dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @PostMapping("/devolucion")
    public ResponseEntity<MovimientoStockResponseDTO> registrarDevolucion(@AuthenticationPrincipal UserModel user, @Valid @RequestBody MovimientoStockDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarDevolucion(user, dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @PostMapping("/ajuste")
    public ResponseEntity<MovimientoStockResponseDTO> registrarAjuste(@AuthenticationPrincipal UserModel user, @Valid @RequestBody AjusteStockDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarAjuste(user, dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @GetMapping("/variante/{varianteId}")
    public ResponseEntity<List<MovimientoStockResponseDTO>> getKardex(@AuthenticationPrincipal UserModel user, @PathVariable Long varianteId) {
        return ResponseEntity.ok(service.getKardexByVariante(user, varianteId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @GetMapping("/bajo")
    public ResponseEntity<List<VarianteProductoResponseDTO>> getStockBajo(@AuthenticationPrincipal UserModel user) {
        return ResponseEntity.ok(service.getStockBajo(user));
    }
}