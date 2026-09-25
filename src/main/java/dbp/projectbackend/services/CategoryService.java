package dbp.projectbackend.services;

import dbp.projectbackend.dtos.CategoryDTO;
import dbp.projectbackend.dtos.CategoryResponseDTO;
import dbp.projectbackend.exceptions.DuplicateResourceException;
import dbp.projectbackend.exceptions.ResourceNotFoundException;
import dbp.projectbackend.exceptions.UnauthorizedException;
import dbp.projectbackend.models.CategoryModel;
import dbp.projectbackend.models.EnterpriseModel;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryResponseDTO createCategory(UserModel currentUser, CategoryDTO dto) {
        EnterpriseModel empresa = currentUser.getEmpresa();

        if (categoryRepository.existsByEmpresaIdAndNombreIgnoreCase(empresa.getId(), dto.nombre())) {
            throw new DuplicateResourceException("Ya existe una categoría con el nombre \"" + dto.nombre() + "\" en esta empresa.");
        }

        CategoryModel newCategory = new CategoryModel(dto.nombre(), empresa);
        newCategory.setDescripcion(dto.descripcion());

        return toDTO(categoryRepository.save(newCategory));
    }

    public List<CategoryResponseDTO> getCategoriesByEnterprise(UserModel currentUser) {
        return categoryRepository.findByEmpresaId(currentUser.getEmpresa().getId()).stream()
                .map(this::toDTO)
                .toList();
    }

    public CategoryResponseDTO getCategoryById(UserModel currentUser, Long id) {
        return toDTO(findOwnedCategory(currentUser, id));
    }

    @Transactional
    public CategoryResponseDTO updateCategory(UserModel currentUser, Long id, CategoryDTO dto) {
        CategoryModel category = findOwnedCategory(currentUser, id);

        if (!category.getNombre().equalsIgnoreCase(dto.nombre())
                && categoryRepository.existsByEmpresaIdAndNombreIgnoreCase(currentUser.getEmpresa().getId(), dto.nombre())) {
            throw new DuplicateResourceException("Ya existe una categoría con el nombre \"" + dto.nombre() + "\" en esta empresa.");
        }

        category.setNombre(dto.nombre());
        category.setDescripcion(dto.descripcion());

        return toDTO(categoryRepository.save(category));
    }

    @Transactional
    public void deactivateCategory(UserModel currentUser, Long id) {
        CategoryModel category = findOwnedCategory(currentUser, id);
        category.setActivo(false);
        categoryRepository.save(category);
    }

    private CategoryModel findOwnedCategory(UserModel currentUser, Long id) {
        CategoryModel category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría con id " + id + " no encontrada."));

        if (!category.getEmpresa().getId().equals(currentUser.getEmpresa().getId())) {
            throw new UnauthorizedException("No tienes acceso a esta categoría.");
        }
        return category;
    }

    private CategoryResponseDTO toDTO(CategoryModel category) {
        return new CategoryResponseDTO(
                category.getId(),
                category.getNombre(),
                category.getDescripcion(),
                category.getActivo(),
                category.getEmpresa().getId()
        );
    }
}