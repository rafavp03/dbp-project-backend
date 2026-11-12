package dbp.projectbackend.controllers;

import dbp.projectbackend.dtos.request.CategoryDTO;
import dbp.projectbackend.dtos.response.CategoryResponseDTO;
import dbp.projectbackend.dtos.response.PageResponseDTO;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.services.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService service;

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(@AuthenticationPrincipal UserModel user, @Valid @RequestBody CategoryDTO dto) {
        CategoryResponseDTO newCategory = service.createCategory(user, dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newCategory.id())
                .toUri();
        return ResponseEntity.created(location).body(newCategory);
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @GetMapping
    public ResponseEntity<PageResponseDTO<CategoryResponseDTO>> getCategories(
            @AuthenticationPrincipal UserModel user,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "nombre") Pageable pageable) {
        return ResponseEntity.ok(service.getCategoriesByEnterprise(user, search, pageable));
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
