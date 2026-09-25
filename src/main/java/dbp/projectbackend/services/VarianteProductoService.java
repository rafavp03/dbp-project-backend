package dbp.projectbackend.services;

import dbp.projectbackend.dtos.VarianteProductoDTO;
import dbp.projectbackend.dtos.VarianteProductoResponseDTO;
import dbp.projectbackend.exceptions.DuplicateResourceException;
import dbp.projectbackend.exceptions.ResourceNotFoundException;
import dbp.projectbackend.exceptions.UnauthorizedException;
import dbp.projectbackend.models.ProductModel;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.models.VarianteProductoModel;
import dbp.projectbackend.repositories.ProductRepository;
import dbp.projectbackend.repositories.VarianteProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class VarianteProductoService {

    private final VarianteProductoRepository varianteRepository;
    private final ProductRepository productRepository;

    @Transactional
    public VarianteProductoResponseDTO createVariante(UserModel currentUser, Long productoId, VarianteProductoDTO dto) {
        ProductModel producto = findOwnedProduct(currentUser, productoId);

        varianteRepository.findByProductoIdAndTallaAndColor(productoId, dto.talla(), dto.color())
                .ifPresent(v -> {
                    throw new DuplicateResourceException(
                            "Ya existe la variante " + dto.talla() + " - " + dto.color() + " para este producto.");
                });

        VarianteProductoModel newVariante = new VarianteProductoModel(dto.talla(), dto.color());
        newVariante.setSku(dto.sku());
        if (dto.stockMinimo() != null) {
            newVariante.setStockMinimo(dto.stockMinimo());
        }
        producto.addVariante(newVariante);

        return toDTO(varianteRepository.save(newVariante));
    }

    @Transactional(readOnly = true)
    public List<VarianteProductoResponseDTO> getVariantesByProducto(UserModel currentUser, Long productoId) {
        findOwnedProduct(currentUser, productoId);
        return varianteRepository.findByProductoId(productoId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public VarianteProductoResponseDTO getVarianteById(UserModel currentUser, Long id) {
        return toDTO(findOwnedVariante(currentUser, id));
    }

    @Transactional
    public VarianteProductoResponseDTO updateVariante(UserModel currentUser, Long id, VarianteProductoDTO dto) {
        VarianteProductoModel variante = findOwnedVariante(currentUser, id);

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
    public void deactivateVariante(UserModel currentUser, Long id) {
        VarianteProductoModel variante = findOwnedVariante(currentUser, id);
        variante.setActivo(false);
        varianteRepository.save(variante);
    }

    private ProductModel findOwnedProduct(UserModel currentUser, Long productoId) {
        ProductModel producto = productRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto con id " + productoId + " no encontrado."));

        if (!producto.getEmpresa().getId().equals(currentUser.getEmpresa().getId())) {
            throw new UnauthorizedException("No tienes acceso a este producto.");
        }
        return producto;
    }

    private VarianteProductoModel findOwnedVariante(UserModel currentUser, Long id) {
        VarianteProductoModel variante = varianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variante con id " + id + " no encontrada."));

        if (!variante.getProducto().getEmpresa().getId().equals(currentUser.getEmpresa().getId())) {
            throw new UnauthorizedException("No tienes acceso a esta variante.");
        }
        return variante;
    }

    private VarianteProductoResponseDTO toDTO(VarianteProductoModel variante) {
        return new VarianteProductoResponseDTO(
                variante.getId(),
                variante.getProducto().getId(),
                variante.getProducto().getNombre(),
                variante.getTalla(),
                variante.getColor(),
                variante.getSku(),
                variante.getStock(),
                variante.getStockMinimo(),
                variante.getActivo(),
                variante.isStockBajo()
        );
    }
}