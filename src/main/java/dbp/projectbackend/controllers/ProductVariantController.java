package dbp.projectbackend.controllers;

import dbp.projectbackend.dtos.request.ProductVariantDTO;
import dbp.projectbackend.dtos.response.ProductVariantResponseDTO;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.services.ProductVariantService;
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
public class ProductVariantController {

    private final ProductVariantService service;

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @PostMapping("/product/{productoId}/variantes")
    public ResponseEntity<ProductVariantResponseDTO> createVariant(
            @AuthenticationPrincipal UserModel user,
            @PathVariable Long productoId,
            @Valid @RequestBody ProductVariantDTO dto) {
        ProductVariantResponseDTO nueva = service.createVariant(user, productoId, dto);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/variante/{id}")
                .buildAndExpand(nueva.id())
                .toUri();
        return ResponseEntity.created(location).body(nueva);
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @GetMapping("/product/{productoId}/variantes")
    public ResponseEntity<List<ProductVariantResponseDTO>> getVariantsByProduct(
            @AuthenticationPrincipal UserModel user,
            @PathVariable Long productoId) {
        return ResponseEntity.ok(service.getVariantsByProduct(user, productoId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @GetMapping("/variante/{id}")
    public ResponseEntity<ProductVariantResponseDTO> getVariant(@AuthenticationPrincipal UserModel user, @PathVariable Long id) {
        return ResponseEntity.ok(service.getVariantById(user, id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @PutMapping("/variante/{id}")
    public ResponseEntity<ProductVariantResponseDTO> updateVariant(
            @AuthenticationPrincipal UserModel user, @PathVariable Long id, @Valid @RequestBody ProductVariantDTO dto) {
        return ResponseEntity.ok(service.updateVariant(user, id, dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/variante/{id}")
    public ResponseEntity<Void> deactivateVariant(@AuthenticationPrincipal UserModel user, @PathVariable Long id) {
        service.deactivateVariant(user, id);
        return ResponseEntity.noContent().build();
    }
}
