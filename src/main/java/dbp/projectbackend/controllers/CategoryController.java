package dbp.projectbackend.controllers;

import dbp.projectbackend.dtos.CategoryDTO;
import dbp.projectbackend.dtos.CategoryResponseDTO;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.services.CategoryService;
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
@RequestMapping("/category")
public class CategoryController {

    private final CategoryService service;

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(@AuthenticationPrincipal UserModel user, @Valid @RequestBody CategoryDTO dto) {
        CategoryResponseDTO newCategory = service.createCategory(user, dto);
        return ResponseEntity
                .created(URI.create("/category/" + newCategory.id()))
                .body(newCategory);
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getCategories(@AuthenticationPrincipal UserModel user) {
        return ResponseEntity.ok(service.getCategoriesByEnterprise(user));
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getCategory(@AuthenticationPrincipal UserModel user, @PathVariable Long id) {
        return ResponseEntity.ok(service.getCategoryById(user, id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(@AuthenticationPrincipal UserModel user, @PathVariable Long id, @Valid @RequestBody CategoryDTO dto) {
        return ResponseEntity.ok(service.updateCategory(user, id, dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateCategory(@AuthenticationPrincipal UserModel user, @PathVariable Long id) {
        service.deactivateCategory(user, id);
        return ResponseEntity.noContent().build();
    }
}