package dbp.projectbackend.services;

import dbp.projectbackend.dtos.request.StockAdjustmentDTO;
import dbp.projectbackend.dtos.request.StockMovementDTO;
import dbp.projectbackend.dtos.response.StockMovementResponseDTO;
import dbp.projectbackend.dtos.response.ProductVariantResponseDTO;
import dbp.projectbackend.exceptions.ResourceNotFoundException;
import dbp.projectbackend.models.StockMovementModel;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.models.ProductVariantModel;
import dbp.projectbackend.repositories.StockMovementRepository;
import dbp.projectbackend.repositories.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class StockMovementService {

    private final StockMovementRepository movimientoStockRepository;
    private final ProductVariantRepository varianteRepository;

    @Transactional
    public StockMovementResponseDTO registerInbound(UserModel currentUser, StockMovementDTO dto) {
        ProductVariantModel variante = findOwnedVariant(currentUser, dto.varianteId());
        StockMovementModel movimiento = StockMovementModel.inbound(variante, dto.cantidad(), dto.motivo());
        return toDTO(movimientoStockRepository.save(movimiento));
    }

    @Transactional
    public StockMovementResponseDTO registerOutbound(UserModel currentUser, StockMovementDTO dto) {
        ProductVariantModel variante = findOwnedVariant(currentUser, dto.varianteId());
        StockMovementModel movimiento = StockMovementModel.outbound(variante, dto.cantidad(), dto.motivo());
        return toDTO(movimientoStockRepository.save(movimiento));
    }

    @Transactional
    public StockMovementResponseDTO registerReturn(UserModel currentUser, StockMovementDTO dto) {
        ProductVariantModel variante = findOwnedVariant(currentUser, dto.varianteId());
        StockMovementModel movimiento = StockMovementModel.customerReturn(variante, dto.cantidad(), dto.motivo());
        return toDTO(movimientoStockRepository.save(movimiento));
    }

    @Transactional
    public StockMovementResponseDTO registerAdjustment(UserModel currentUser, StockAdjustmentDTO dto) {
        ProductVariantModel variante = findOwnedVariant(currentUser, dto.varianteId());
        StockMovementModel movimiento = StockMovementModel.adjustment(variante, dto.nuevoStock(), dto.motivo());
        return toDTO(movimientoStockRepository.save(movimiento));
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponseDTO> getKardexByVariant(UserModel currentUser, Long varianteId) {
        findOwnedVariant(currentUser, varianteId);
        return movimientoStockRepository.findByVarianteIdOrderByFechaDesc(varianteId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductVariantResponseDTO> getLowStock(UserModel currentUser) {
        return varianteRepository.findStockBajo(currentUser.getEmpresa().getId()).stream()
                .map(v -> new ProductVariantResponseDTO(
                        v.getId(),
                        v.getProducto().getId(),
                        v.getProducto().getNombre(),
                        v.getTalla(),
                        v.getColor(),
                        v.getSku(),
                        v.getStock(),
                        v.getStockMinimo(),
                        v.getActivo(),
                        v.isLowStock()
                ))
                .toList();
    }

    private ProductVariantModel findOwnedVariant(UserModel currentUser, Long varianteId) {
        ProductVariantModel variante = varianteRepository.findById(varianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Variante con id " + varianteId + " no encontrada."));

        if (!variante.getProducto().getEmpresa().getId().equals(currentUser.getEmpresa().getId())) {
            throw new ResourceNotFoundException("Variante con id " + varianteId + " no encontrada.");
        }
        return variante;
    }

    private StockMovementResponseDTO toDTO(StockMovementModel movimiento) {
        return new StockMovementResponseDTO(
                movimiento.getId(),
                movimiento.getVariante().getId(),
                movimiento.getVariante().getFullName(),
                movimiento.getTipo(),
                movimiento.getCantidad(),
                movimiento.getStockAnterior(),
                movimiento.getStockResultante(),
                movimiento.getMotivo(),
                movimiento.getFecha()
        );
    }
}
