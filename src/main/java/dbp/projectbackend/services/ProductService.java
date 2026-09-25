package dbp.projectbackend.services;

import dbp.projectbackend.dtos.ProductDTO;
import dbp.projectbackend.dtos.ProductResponseDTO;
import dbp.projectbackend.exceptions.DuplicateResourceException;
import dbp.projectbackend.exceptions.ResourceNotFoundException;
import dbp.projectbackend.exceptions.UnauthorizedException;
import dbp.projectbackend.models.CategoryModel;
import dbp.projectbackend.models.EnterpriseModel;
import dbp.projectbackend.models.ProductModel;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.repositories.CategoryRepository;
import dbp.projectbackend.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public ProductResponseDTO createProduct(UserModel currentUser, ProductDTO dto) {
        EnterpriseModel empresa = currentUser.getEmpresa();

        if (productRepository.existsByEmpresaIdAndCodigo(empresa.getId(), dto.codigo())) {
            throw new DuplicateResourceException("Ya existe un producto con código \"" + dto.codigo() + "\" en esta empresa.");
        }

        CategoryModel categoria = resolveCategoria(empresa, dto.categoriaId());

        ProductModel newProduct = new ProductModel(dto.codigo(), dto.nombre(), dto.precioVenta(), empresa);
        newProduct.setDescripcion(dto.descripcion());
        newProduct.setPrecioCompra(dto.precioCompra());
        if (dto.unidadMedida() != null && !dto.unidadMedida().isBlank()) {
            newProduct.setUnidadMedida(dto.unidadMedida());
        }
        newProduct.setCategoria(categoria);

        return toDTO(productRepository.save(newProduct));
    }

    public List<ProductResponseDTO> getProductsByEnterprise(UserModel currentUser, boolean soloActivos) {
        Long empresaId = currentUser.getEmpresa().getId();
        List<ProductModel> productos = soloActivos
                ? productRepository.findByEmpresaIdAndActivoTrue(empresaId)
                : productRepository.findByEmpresaId(empresaId);

        return productos.stream().map(this::toDTO).toList();
    }

    public ProductResponseDTO getProductById(UserModel currentUser, Long id) {
        return toDTO(findOwnedProduct(currentUser, id));
    }

    @Transactional
    public ProductResponseDTO updateProduct(UserModel currentUser, Long id, ProductDTO dto) {
        ProductModel product = findOwnedProduct(currentUser, id);

        if (!product.getCodigo().equals(dto.codigo())
                && productRepository.existsByEmpresaIdAndCodigo(currentUser.getEmpresa().getId(), dto.codigo())) {
            throw new DuplicateResourceException("Ya existe un producto con código \"" + dto.codigo() + "\" en esta empresa.");
        }

        CategoryModel categoria = resolveCategoria(currentUser.getEmpresa(), dto.categoriaId());

        product.setCodigo(dto.codigo());
        product.setNombre(dto.nombre());
        product.setDescripcion(dto.descripcion());
        product.setPrecioCompra(dto.precioCompra());
        product.setPrecioVenta(dto.precioVenta());
        if (dto.unidadMedida() != null && !dto.unidadMedida().isBlank()) {
            product.setUnidadMedida(dto.unidadMedida());
        }
        product.setCategoria(categoria);

        return toDTO(productRepository.save(product));
    }

    @Transactional
    public void deactivateProduct(UserModel currentUser, Long id) {
        ProductModel product = findOwnedProduct(currentUser, id);
        product.setActivo(false);
        productRepository.save(product);
    }

    private CategoryModel resolveCategoria(EnterpriseModel empresa, Long categoriaId) {
        if (categoriaId == null) return null;

        CategoryModel categoria = categoryRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría con id " + categoriaId + " no encontrada."));

        if (!categoria.getEmpresa().getId().equals(empresa.getId())) {
            throw new UnauthorizedException("La categoría no pertenece a tu empresa.");
        }
        return categoria;
    }

    private ProductModel findOwnedProduct(UserModel currentUser, Long id) {
        ProductModel product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto con id " + id + " no encontrado."));

        if (!product.getEmpresa().getId().equals(currentUser.getEmpresa().getId())) {
            throw new UnauthorizedException("No tienes acceso a este producto.");
        }
        return product;
    }

    private ProductResponseDTO toDTO(ProductModel product) {
        return new ProductResponseDTO(
                product.getId(),
                product.getCodigo(),
                product.getNombre(),
                product.getDescripcion(),
                product.getUnidadMedida(),
                product.getPrecioCompra(),
                product.getPrecioVenta(),
                product.getActivo(),
                product.getCategoria() != null ? product.getCategoria().getId() : null,
                product.getCategoria() != null ? product.getCategoria().getNombre() : null,
                product.getEmpresa().getId(),
                product.getStockTotal()
        );
    }
}