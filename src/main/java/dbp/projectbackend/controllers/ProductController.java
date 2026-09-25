package dbp.projectbackend.controllers;

import dbp.projectbackend.dtos.ProductDTO;
import dbp.projectbackend.dtos.ProductResponseDTO;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/product")
public class ProductController {

    private final ProductService service;

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(@AuthenticationPrincipal UserModel user, @Valid @RequestBody ProductDTO dto) {
        ProductResponseDTO newProduct = service.createProduct(user, dto);
        return ResponseEntity
                .created(URI.create("/product/" + newProduct.id()))
                .body(newProduct);
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getProducts(
            @AuthenticationPrincipal UserModel user,
            @RequestParam(defaultValue = "true") boolean soloActivos) {
        return ResponseEntity.ok(service.getProductsByEnterprise(user, soloActivos));
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProduct(@AuthenticationPrincipal UserModel user, @PathVariable Long id) {
        return ResponseEntity.ok(service.getProductById(user, id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(@AuthenticationPrincipal UserModel user, @PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
        return ResponseEntity.ok(service.updateProduct(user, id, dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateProduct(@AuthenticationPrincipal UserModel user, @PathVariable Long id) {
        service.deactivateProduct(user, id);
        return ResponseEntity.noContent().build();
    }
}