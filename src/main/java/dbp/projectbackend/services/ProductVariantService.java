package dbp.projectbackend.services;

import dbp.projectbackend.dtos.request.ProductVariantDTO;
import dbp.projectbackend.dtos.response.PageResponseDTO;
import dbp.projectbackend.dtos.response.ProductVariantResponseDTO;
import dbp.projectbackend.exceptions.DuplicateResourceException;
import dbp.projectbackend.exceptions.ResourceNotFoundException;
import dbp.projectbackend.mappers.ProductVariantMapper;
import dbp.projectbackend.models.ProductModel;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.models.ProductVariantModel;
import dbp.projectbackend.repositories.ProductRepository;
import dbp.projectbackend.repositories.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ProductVariantService {

    private final ProductVariantRepository varianteRepository;
    private final ProductRepository productRepository;
    private final ProductVariantMapper varianteMapper;

    @Transactional
    public ProductVariantResponseDTO createVariant(UserModel currentUser, Long productoId, ProductVariantDTO dto) {
        ProductModel producto = findOwnedProduct(currentUser, productoId);

        varianteRepository.findByProductoIdAndTallaAndColor(productoId, dto.talla(), dto.color())
                .ifPresent(v -> {
                    throw new DuplicateResourceException(
                            "Ya existe la variante " + dto.talla() + " - " + dto.color() + " para este producto.");
                });

        ProductVariantModel newVariante = new ProductVariantModel(dto.talla(), dto.color());
        varianteMapper.updateEntity(dto, newVariante);
        producto.addVariant(newVariante);

        return varianteMapper.toResponse(varianteRepository.save(newVariante));
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<ProductVariantResponseDTO> getVariantsByProduct(UserModel currentUser, Long productoId,
                                                                            Pageable pageable) {
        findOwnedProduct(currentUser, productoId);
        return PageResponseDTO.from(
                varianteRepository.findByProductoId(productoId, pageable).map(varianteMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public ProductVariantResponseDTO getVariantById(UserModel currentUser, Long id) {
        return varianteMapper.toResponse(findOwnedVariant(currentUser, id));
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

        varianteMapper.updateEntity(dto, variante);

        return varianteMapper.toResponse(varianteRepository.save(variante));
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
}
