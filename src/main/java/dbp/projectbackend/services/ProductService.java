package dbp.projectbackend.services;

import dbp.projectbackend.dtos.request.ProductDTO;
import dbp.projectbackend.dtos.request.ProductPatchDTO;
import dbp.projectbackend.dtos.response.PageResponseDTO;
import dbp.projectbackend.dtos.response.ProductResponseDTO;
import dbp.projectbackend.events.ProductPriceChangedEvent;
import dbp.projectbackend.exceptions.DuplicateResourceException;
import dbp.projectbackend.exceptions.ForbiddenException;
import dbp.projectbackend.exceptions.ResourceNotFoundException;
import dbp.projectbackend.mappers.ProductMapper;
import dbp.projectbackend.models.CategoryModel;
import dbp.projectbackend.models.EnterpriseModel;
import dbp.projectbackend.models.ProductModel;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.repositories.CategoryRepository;
import dbp.projectbackend.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ProductResponseDTO createProduct(UserModel currentUser, ProductDTO dto) {
        EnterpriseModel empresa = currentUser.getEmpresa();

        if (productRepository.existsByEmpresaIdAndCodigo(empresa.getId(), dto.codigo())) {
            throw new DuplicateResourceException("Ya existe un producto con código \"" + dto.codigo() + "\" en esta empresa.");
        }

        ProductModel newProduct = new ProductModel(dto.codigo(), dto.nombre(), dto.precioVenta(), empresa);
        productMapper.updateEntity(dto, newProduct);
        newProduct.setCategoria(resolveCategory(empresa, dto.categoriaId()));

        return productMapper.toResponse(productRepository.save(newProduct));
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<ProductResponseDTO> getProductsByEnterprise(UserModel currentUser, boolean soloActivos,
                                                                       String search, Pageable pageable) {
        Long empresaId = currentUser.getEmpresa().getId();
        Page<ProductModel> productos;

        if (search != null && !search.isBlank()) {
            productos = productRepository.searchByEmpresaId(empresaId, soloActivos, search.trim(), pageable);
        } else if (soloActivos) {
            productos = productRepository.findByEmpresaIdAndActivoTrue(empresaId, pageable);
        } else {
            productos = productRepository.findByEmpresaId(empresaId, pageable);
        }

        return PageResponseDTO.from(productos.map(productMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(UserModel currentUser, Long id) {
        return productMapper.toResponse(findOwnedProduct(currentUser, id));
    }

    @Transactional
    public ProductResponseDTO updateProduct(UserModel currentUser, Long id, ProductDTO dto) {
        ProductModel product = findOwnedProduct(currentUser, id);

        if (!product.getCodigo().equals(dto.codigo())
                && productRepository.existsByEmpresaIdAndCodigo(currentUser.getEmpresa().getId(), dto.codigo())) {
            throw new DuplicateResourceException("Ya existe un producto con código \"" + dto.codigo() + "\" en esta empresa.");
        }

        PriceSnapshot antes = PriceSnapshot.of(product);
        productMapper.updateEntity(dto, product);
        product.setCategoria(resolveCategory(currentUser.getEmpresa(), dto.categoriaId()));

        ProductModel guardado = productRepository.save(product);
        publishIfPriceChanged(currentUser, guardado, antes);
        return productMapper.toResponse(guardado);
    }

    @Transactional
    public ProductResponseDTO patchProduct(UserModel currentUser, Long id, ProductPatchDTO dto) {
        ProductModel product = findOwnedProduct(currentUser, id);

        if (dto.codigo() != null
                && !product.getCodigo().equals(dto.codigo())
                && productRepository.existsByEmpresaIdAndCodigo(currentUser.getEmpresa().getId(), dto.codigo())) {
            throw new DuplicateResourceException(
                    "Ya existe un producto con código \"" + dto.codigo() + "\" en esta empresa.");
        }

        PriceSnapshot antes = PriceSnapshot.of(product);
        productMapper.patchEntity(dto, product);
        if (dto.categoriaId() != null) {
            product.setCategoria(resolveCategory(currentUser.getEmpresa(), dto.categoriaId()));
        }

        ProductModel guardado = productRepository.save(product);
        publishIfPriceChanged(currentUser, guardado, antes);
        return productMapper.toResponse(guardado);
    }

    @Transactional
    public void deactivateProduct(UserModel currentUser, Long id) {
        ProductModel product = findOwnedProduct(currentUser, id);
        product.setActivo(false);
        productRepository.save(product);
    }

    private void publishIfPriceChanged(UserModel currentUser, ProductModel product, PriceSnapshot antes) {
        boolean cambioVenta = differs(antes.precioVenta(), product.getPrecioVenta());
        boolean cambioCosto = differs(antes.precioCompra(), product.getPrecioCompra());
        if (!cambioVenta && !cambioCosto) {
            return;
        }
        eventPublisher.publishEvent(new ProductPriceChangedEvent(
                this,
                product.getId(),
                product.getEmpresa().getId(),
                product.getCodigo(),
                product.getNombre(),
                antes.precioVenta(),
                product.getPrecioVenta(),
                antes.precioCompra(),
                product.getPrecioCompra(),
                currentUser.getNombre()));
    }

    /** Compara montos por valor: 25.0 y 25.00 son el mismo precio. */
    private static boolean differs(BigDecimal anterior, BigDecimal nuevo) {
        if (anterior == null || nuevo == null) {
            return anterior != nuevo;
        }
        return anterior.compareTo(nuevo) != 0;
    }

    private CategoryModel resolveCategory(EnterpriseModel empresa, Long categoriaId) {
        if (categoriaId == null) return null;

        CategoryModel categoria = categoryRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría con id " + categoriaId + " no encontrada."));

        if (!categoria.getEmpresa().getId().equals(empresa.getId())) {
            throw new ForbiddenException("La categoría no pertenece a tu empresa.");
        }
        return categoria;
    }

    private ProductModel findOwnedProduct(UserModel currentUser, Long id) {
        ProductModel product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto con id " + id + " no encontrado."));

        if (!product.getEmpresa().getId().equals(currentUser.getEmpresa().getId())) {
            throw new ResourceNotFoundException("Producto con id " + id + " no encontrado.");
        }
        return product;
    }

    private record PriceSnapshot(BigDecimal precioVenta, BigDecimal precioCompra) {
        static PriceSnapshot of(ProductModel product) {
            return new PriceSnapshot(product.getPrecioVenta(), product.getPrecioCompra());
        }
    }
}