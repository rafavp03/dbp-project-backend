package dbp.projectbackend.services;

import dbp.projectbackend.dtos.request.PurchaseOrderLineDTO;
import dbp.projectbackend.dtos.request.PurchaseOrderDTO;
import dbp.projectbackend.dtos.response.PageResponseDTO;
import dbp.projectbackend.dtos.response.PurchaseOrderResponseDTO;
import dbp.projectbackend.enums.OrderStatus;
import dbp.projectbackend.events.PurchaseRegisteredEvent;
import dbp.projectbackend.exceptions.ResourceNotFoundException;
import dbp.projectbackend.mappers.PurchaseOrderMapper;
import dbp.projectbackend.models.*;
import dbp.projectbackend.repositories.StockMovementRepository;
import dbp.projectbackend.repositories.PurchaseOrderRepository;
import dbp.projectbackend.repositories.SupplierRepository;
import dbp.projectbackend.repositories.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository ordenCompraRepository;
    private final SupplierRepository supplierRepository;
    private final ProductVariantRepository varianteRepository;
    private final StockMovementRepository movimientoStockRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PurchaseOrderMapper purchaseOrderMapper;

    @Transactional
    public PurchaseOrderResponseDTO createPurchaseOrder(UserModel currentUser, PurchaseOrderDTO dto) {
        EnterpriseModel empresa = currentUser.getEmpresa();

        SupplierModel proveedor = supplierRepository.findById(dto.proveedorId())
                .filter(p -> p.getEmpresas().stream().anyMatch(e -> e.getId().equals(empresa.getId())))
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor con id " + dto.proveedorId() + " no encontrado"));

        PurchaseOrderModel ordenCompra = new PurchaseOrderModel(empresa, proveedor);
        List<StockMovementModel> movimientos = new ArrayList<>();

        for (PurchaseOrderLineDTO detalleDto : dto.detalles()) {
            ProductVariantModel variante = varianteRepository.findById(detalleDto.varianteId())
                    .filter(v -> v.getProducto().getEmpresa().getId().equals(empresa.getId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Variante con id " + detalleDto.varianteId() + " no encontrada"));

            ordenCompra.addLine(new PurchaseOrderLineModel(variante, detalleDto.cantidad(), detalleDto.precioUnitario()));
            movimientos.add(StockMovementModel.inbound(variante, detalleDto.cantidad(), "Compra"));
        }

        ordenCompra.recalculateTotal();
        ordenCompra.setEstado(OrderStatus.COMPLETADA);
        PurchaseOrderModel guardada = ordenCompraRepository.save(ordenCompra);

        movimientos.forEach(m -> m.setOrdenCompra(guardada));
        movimientoStockRepository.saveAll(movimientos);

        eventPublisher.publishEvent(new PurchaseRegisteredEvent(
                this,
                guardada.getId(),
                empresa.getId(),
                proveedor.getRazonSocial(),
                guardada.getFechaEmision(),
                guardada.getTotal(),
                guardada.getDetalles().stream()
                        .map(d -> new PurchaseRegisteredEvent.PurchaseLine(
                                d.getVariante().getFullName(),
                                d.getCantidad(),
                                d.getPrecioUnitario(),
                                d.getSubtotal()))
                        .toList()
        ));

        return purchaseOrderMapper.toResponse(guardada);
    }

    /**
     * Lista las compras de la empresa, por defecto las más recientes primero.
     * Si se envía {@code desde} y/o {@code hasta}, filtra por fecha de emisión (ambos inclusive).
     */
    @Transactional(readOnly = true)
    public PageResponseDTO<PurchaseOrderResponseDTO> getPurchaseOrders(UserModel currentUser, LocalDate desde,
                                                                       LocalDate hasta, Pageable pageable) {
        Long empresaId = currentUser.getEmpresa().getId();
        Page<PurchaseOrderModel> compras;
        if (desde == null && hasta == null) {
            compras = ordenCompraRepository.findByEmpresaId(empresaId, pageable);
        } else {
            DateRange rango = DateRange.of(desde, hasta);
            compras = ordenCompraRepository.findByEmpresaIdAndFechaEmisionGreaterThanEqualAndFechaEmisionLessThan(
                    empresaId, rango.inicio(), rango.finExclusivo(), pageable);
        }
        return PageResponseDTO.from(compras.map(purchaseOrderMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponseDTO getPurchaseOrderById(UserModel currentUser, Long id) {
        PurchaseOrderModel orden = ordenCompraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden de compra con id " + id + " no encontrada"));

        if (!orden.getEmpresa().getId().equals(currentUser.getEmpresa().getId())) {
            throw new ResourceNotFoundException("Orden de compra con id " + id + " no encontrada");
        }

        return purchaseOrderMapper.toResponse(orden);
    }
}
