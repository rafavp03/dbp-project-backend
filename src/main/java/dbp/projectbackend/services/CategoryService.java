package dbp.projectbackend.services;

import dbp.projectbackend.dtos.request.CategoryDTO;
import dbp.projectbackend.dtos.response.CategoryResponseDTO;
import dbp.projectbackend.dtos.response.PageResponseDTO;
import dbp.projectbackend.exceptions.DuplicateResourceException;
import dbp.projectbackend.exceptions.ResourceNotFoundException;
import dbp.projectbackend.mappers.CategoryMapper;
import dbp.projectbackend.models.CategoryModel;
import dbp.projectbackend.models.EnterpriseModel;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    public CategoryResponseDTO createCategory(UserModel currentUser, CategoryDTO dto) {
        EnterpriseModel empresa = currentUser.getEmpresa();

        if (categoryRepository.existsByEmpresaIdAndNombreIgnoreCase(empresa.getId(), dto.nombre())) {
            throw new DuplicateResourceException("Ya existe una categoría con el nombre \"" + dto.nombre() + "\" en esta empresa.");
        }

        CategoryModel newCategory = new CategoryModel(dto.nombre(), empresa);
        categoryMapper.updateEntity(dto, newCategory);

        return categoryMapper.toResponse(categoryRepository.save(newCategory));
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<CategoryResponseDTO> getCategoriesByEnterprise(UserModel currentUser, Pageable pageable) {
        return PageResponseDTO.from(
                categoryRepository.findByEmpresaId(currentUser.getEmpresa().getId(), pageable)
                        .map(categoryMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategoryById(UserModel currentUser, Long id) {
        return categoryMapper.toResponse(findOwnedCategory(currentUser, id));
    }

    @Transactional
    public CategoryResponseDTO updateCategory(UserModel currentUser, Long id, CategoryDTO dto) {
        CategoryModel category = findOwnedCategory(currentUser, id);

        if (!category.getNombre().equalsIgnoreCase(dto.nombre())
                && categoryRepository.existsByEmpresaIdAndNombreIgnoreCase(currentUser.getEmpresa().getId(), dto.nombre())) {
            throw new DuplicateResourceException("Ya existe una categoría con el nombre \"" + dto.nombre() + "\" en esta empresa.");
        }

        categoryMapper.updateEntity(dto, category);

        return categoryMapper.toResponse(categoryRepository.save(category));
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
            throw new ResourceNotFoundException("Categoría con id " + id + " no encontrada.");
        }
        return category;
    }
}
