package dbp.projectbackend.services;

import dbp.projectbackend.dtos.request.StockAdjustmentDTO;
import dbp.projectbackend.dtos.request.StockMovementDTO;
import dbp.projectbackend.dtos.response.PageResponseDTO;
import dbp.projectbackend.dtos.response.ProductVariantResponseDTO;
import dbp.projectbackend.dtos.response.StockMovementResponseDTO;
import dbp.projectbackend.events.StockMovementRegisteredEvent;
import dbp.projectbackend.exceptions.ResourceNotFoundException;
import dbp.projectbackend.mappers.ProductVariantMapper;
import dbp.projectbackend.mappers.StockMovementMapper;
import dbp.projectbackend.models.ProductVariantModel;
import dbp.projectbackend.models.StockMovementModel;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.repositories.ProductVariantRepository;
import dbp.projectbackend.repositories.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class StockMovementService {

    private final StockMovementRepository movimientoStockRepository;
    private final ProductVariantRepository varianteRepository;
    private final StockMovementMapper movimientoMapper;
    private final ProductVariantMapper varianteMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public StockMovementResponseDTO registerInbound(UserModel currentUser, StockMovementDTO dto) {
        ProductVariantModel variante = findOwnedVariant(currentUser, dto.varianteId());
        return register(currentUser, StockMovementModel.inbound(variante, dto.cantidad(), dto.motivo()));
    }

    @Transactional
    public StockMovementResponseDTO registerOutbound(UserModel currentUser, StockMovementDTO dto) {
        ProductVariantModel variante = findOwnedVariant(currentUser, dto.varianteId());
        return register(currentUser, StockMovementModel.outbound(variante, dto.cantidad(), dto.motivo()));
    }

    @Transactional
    public StockMovementResponseDTO registerReturn(UserModel currentUser, StockMovementDTO dto) {
        ProductVariantModel variante = findOwnedVariant(currentUser, dto.varianteId());
        return register(currentUser, StockMovementModel.customerReturn(variante, dto.cantidad(), dto.motivo()));
    }

    @Transactional
    public StockMovementResponseDTO registerAdjustment(UserModel currentUser, StockAdjustmentDTO dto) {
        ProductVariantModel variante = findOwnedVariant(currentUser, dto.varianteId());
        return register(currentUser, StockMovementModel.adjustment(variante, dto.nuevoStock(), dto.motivo()));
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<StockMovementResponseDTO> getKardexByVariant(UserModel currentUser, Long varianteId,
                                                                        Pageable pageable) {
        findOwnedVariant(currentUser, varianteId);
        return PageResponseDTO.from(
                movimientoStockRepository.findByVarianteId(varianteId, pageable).map(movimientoMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<ProductVariantResponseDTO> getLowStock(UserModel currentUser, Pageable pageable) {
        return PageResponseDTO.from(
                varianteRepository.findStockBajo(currentUser.getEmpresa().getId(), pageable)
                        .map(varianteMapper::toResponse));
    }

    /**
     * Guarda el movimiento y publica el evento. Los listeners (alerta de stock bajo y aviso de
     * ajuste al ADMIN) corren después del commit, así que un rollback no genera correos.
     */
    private StockMovementResponseDTO register(UserModel currentUser, StockMovementModel movimiento) {
        StockMovementModel guardado = movimientoStockRepository.save(movimiento);
        ProductVariantModel variante = guardado.getVariante();

        eventPublisher.publishEvent(new StockMovementRegisteredEvent(
                this,
                guardado.getId(),
                currentUser.getEmpresa().getId(),
                variante.getId(),
                variante.getFullName(),
                guardado.getTipo(),
                guardado.getCantidad(),
                guardado.getStockAnterior(),
                guardado.getStockResultante(),
                guardado.getMotivo(),
                currentUser.getNombre()));

        return movimientoMapper.toResponse(guardado);
    }

    private ProductVariantModel findOwnedVariant(UserModel currentUser, Long varianteId) {
        ProductVariantModel variante = varianteRepository.findById(varianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Variante con id " + varianteId + " no encontrada."));

        if (!variante.getProducto().getEmpresa().getId().equals(currentUser.getEmpresa().getId())) {
            throw new ResourceNotFoundException("Variante con id " + varianteId + " no encontrada.");
        }
        return variante;
    }
}
