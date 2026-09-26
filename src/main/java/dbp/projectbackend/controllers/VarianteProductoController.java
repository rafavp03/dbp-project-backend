package dbp.projectbackend.controllers;

import dbp.projectbackend.dtos.request.VarianteProductoDTO;
import dbp.projectbackend.dtos.response.VarianteProductoResponseDTO;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.services.VarianteProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class VarianteProductoController {

    private final VarianteProductoService service;

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @PostMapping("/product/{productoId}/variantes")
    public ResponseEntity<VarianteProductoResponseDTO> createVariante(
            @AuthenticationPrincipal UserModel user,
            @PathVariable Long productoId,
            @Valid @RequestBody VarianteProductoDTO dto) {
        VarianteProductoResponseDTO nueva = service.createVariante(user, productoId, dto);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/variante/{id}")
                .buildAndExpand(nueva.id())
                .toUri();
        return ResponseEntity.created(location).body(nueva);
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @GetMapping("/product/{productoId}/variantes")
    public ResponseEntity<List<VarianteProductoResponseDTO>> getVariantesByProducto(
            @AuthenticationPrincipal UserModel user,
            @PathVariable Long productoId) {
        return ResponseEntity.ok(service.getVariantesByProducto(user, productoId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @GetMapping("/variante/{id}")
    public ResponseEntity<VarianteProductoResponseDTO> getVariante(@AuthenticationPrincipal UserModel user, @PathVariable Long id) {
        return ResponseEntity.ok(service.getVarianteById(user, id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @PutMapping("/variante/{id}")
    public ResponseEntity<VarianteProductoResponseDTO> updateVariante(
            @AuthenticationPrincipal UserModel user, @PathVariable Long id, @Valid @RequestBody VarianteProductoDTO dto) {
        return ResponseEntity.ok(service.updateVariante(user, id, dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/variante/{id}")
    public ResponseEntity<Void> deactivateVariante(@AuthenticationPrincipal UserModel user, @PathVariable Long id) {
        service.deactivateVariante(user, id);
        return ResponseEntity.noContent().build();
    }
}
