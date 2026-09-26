package dbp.projectbackend.services;

import dbp.projectbackend.dtos.request.ProductVariantDTO;
import dbp.projectbackend.dtos.response.ProductVariantResponseDTO;
import dbp.projectbackend.exceptions.DuplicateResourceException;
import dbp.projectbackend.exceptions.ResourceNotFoundException;
import dbp.projectbackend.models.ProductModel;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.models.ProductVariantModel;
import dbp.projectbackend.repositories.ProductRepository;
import dbp.projectbackend.repositories.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductVariantService {

    private final ProductVariantRepository varianteRepository;
    private final ProductRepository productRepository;

    @Transactional
    public ProductVariantResponseDTO createVariant(UserModel currentUser, Long productoId, ProductVariantDTO dto) {
        ProductModel producto = findOwnedProduct(currentUser, productoId);

        varianteRepository.findByProductoIdAndTallaAndColor(productoId, dto.talla(), dto.color())
                .ifPresent(v -> {
                    throw new DuplicateResourceException(
                            "Ya existe la variante " + dto.talla() + " - " + dto.color() + " para este producto.");
                });

        ProductVariantModel newVariante = new ProductVariantModel(dto.talla(), dto.color());
        newVariante.setSku(dto.sku());
        if (dto.stockMinimo() != null) {
            newVariante.setStockMinimo(dto.stockMinimo());
        }
        producto.addVariant(newVariante);

        return toDTO(varianteRepository.save(newVariante));
    }

    @Transactional(readOnly = true)
    public List<ProductVariantResponseDTO> getVariantsByProduct(UserModel currentUser, Long productoId) {
        findOwnedProduct(currentUser, productoId);
        return varianteRepository.findByProductoId(productoId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductVariantResponseDTO getVariantById(UserModel currentUser, Long id) {
        return toDTO(findOwnedVariant(currentUser, id));
    }

    @Transactional
    public ProductVariantResponseDTO updateVariant(UserModel currentUser, Long id, ProductVariantDTO dto) {
        ProductVariantModel variante = findOwnedVariant(currentUser, id);

        boolean cambioTallaOColor = !variante.getTalla().equals(dto.talla()) || !variante.getColor().equals(dto.color());
        if (cambioTallaOColor) {
            varianteRepository.findByProductoIdAndTallaAndColor(variante.getProducto().getId(), dto.talla(), dto.color())
                    .ifPresent(v -> {
                        throw new DuplicateResourceException(
                                "Ya existe la variante " + dto.talla() + " - " + dto.color() + " para este producto.");
                    });
        }

        variante.setTalla(dto.talla());
        variante.setColor(dto.color());
        variante.setSku(dto.sku());
        if (dto.stockMinimo() != null) {
            variante.setStockMinimo(dto.stockMinimo());
        }

        return toDTO(varianteRepository.save(variante));
    }

    @Transactional
    public void deactivateVariant(UserModel currentUser, Long id) {
        ProductVariantModel variante = findOwnedVariant(currentUser, id);
        variante.setActivo(false);
        varianteRepository.save(variante);
    }

    private ProductModel findOwnedProduct(UserModel currentUser, Long productoId) {
        ProductModel producto = productRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto con id " + productoId + " no encontrado."));

        if (!producto.getEmpresa().getId().equals(currentUser.getEmpresa().getId())) {
            throw new ResourceNotFoundException("Producto con id " + productoId + " no encontrado.");
        }
        return producto;
    }

    private ProductVariantModel findOwnedVariant(UserModel currentUser, Long id) {
        ProductVariantModel variante = varianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variante con id " + id + " no encontrada."));

        if (!variante.getProducto().getEmpresa().getId().equals(currentUser.getEmpresa().getId())) {
            throw new ResourceNotFoundException("Variante con id " + id + " no encontrada.");
        }
        return variante;
    }

    private ProductVariantResponseDTO toDTO(ProductVariantModel variante) {
        return new ProductVariantResponseDTO(
                variante.getId(),
                variante.getProducto().getId(),
                variante.getProducto().getNombre(),
                variante.getTalla(),
                variante.getColor(),
                variante.getSku(),
                variante.getStock(),
                variante.getStockMinimo(),
                variante.getActivo(),
                variante.isLowStock()
        );
    }
}
