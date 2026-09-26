package dbp.projectbackend.controllers;

import dbp.projectbackend.dtos.request.ProductDTO;
import dbp.projectbackend.dtos.response.ProductResponseDTO;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.services.ProductService;
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
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService service;

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(@AuthenticationPrincipal UserModel user, @Valid @RequestBody ProductDTO dto) {
        ProductResponseDTO newProduct = service.createProduct(user, dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newProduct.id())
                .toUri();
        return ResponseEntity.created(location).body(newProduct);
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
