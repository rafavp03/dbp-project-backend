package dbp.projectbackend.services;

import dbp.projectbackend.dtos.request.SalesOrderLineDTO;
import dbp.projectbackend.dtos.request.SalesOrderDTO;
import dbp.projectbackend.dtos.response.SalesOrderLineResponseDTO;
import dbp.projectbackend.dtos.response.SalesOrderResponseDTO;
import dbp.projectbackend.enums.OrderStatus;
import dbp.projectbackend.events.SaleRegisteredEvent;
import dbp.projectbackend.exceptions.ResourceNotFoundException;
import dbp.projectbackend.models.*;
import dbp.projectbackend.repositories.ClientRepository;
import dbp.projectbackend.repositories.StockMovementRepository;
import dbp.projectbackend.repositories.SalesOrderRepository;
import dbp.projectbackend.repositories.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class SalesOrderService {

    private final SalesOrderRepository ordenVentaRepository;
    private final ClientRepository clientRepository;
    private final ProductVariantRepository varianteRepository;
    private final StockMovementRepository movimientoStockRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public SalesOrderResponseDTO createSalesOrder(UserModel currentUser, SalesOrderDTO dto){
        EnterpriseModel empresa = currentUser.getEmpresa();
        SalesOrderModel ordenVenta = new SalesOrderModel(
                empresa, resolveClient(empresa, dto.clienteId()), dto.medioPago(), dto.canal());

        List<StockMovementModel> movimientos = buildLines(ordenVenta, empresa, dto.detalles());

        ordenVenta.recalculateTotal();
        ordenVenta.setEstado(OrderStatus.COMPLETADA);
        SalesOrderModel guardada = ordenVentaRepository.save(ordenVenta);

        movimientos.forEach(m -> m.setOrdenVenta(guardada));
        movimientoStockRepository.saveAll(movimientos);

        publishSaleRegistered(guardada, empresa, movimientos);
        return toDTO(guardada);
    }

    private ClientModel resolveClient(EnterpriseModel empresa, Long clienteId) {
        if (clienteId == null) {
            return null;
        }
        return clientRepository.findById(clienteId)
                .filter(c -> c.getEmpresa().getId().equals(empresa.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Cliente con id " + clienteId + " no encontrado"));
    }

    private List<StockMovementModel> buildLines(SalesOrderModel ordenVenta, EnterpriseModel empresa,
                                                List<SalesOrderLineDTO> detalles) {
        List<StockMovementModel> movimientos = new ArrayList<>();
        for (SalesOrderLineDTO detalleDto : detalles) {
            ProductVariantModel variante = findOwnedVariant(empresa, detalleDto.varianteId());
            ordenVenta.addLine(new SalesOrderLineModel(
                    variante, detalleDto.cantidad(), salePrice(detalleDto, variante)));
            movimientos.add(StockMovementModel.outbound(variante, detalleDto.cantidad(), "Venta"));
        }
        return movimientos;
    }

    private ProductVariantModel findOwnedVariant(EnterpriseModel empresa, Long varianteId) {
        return varianteRepository.findById(varianteId)
                .filter(v -> v.getProducto().getEmpresa().getId().equals(empresa.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Variante con id " + varianteId + " no encontrada"));
    }

    private BigDecimal salePrice(SalesOrderLineDTO detalleDto, ProductVariantModel variante) {
        return detalleDto.precioUnitario() != null
                ? detalleDto.precioUnitario()
                : variante.getProducto().getPrecioVenta();
    }

    private void publishSaleRegistered(SalesOrderModel guardada, EnterpriseModel empresa,
                                       List<StockMovementModel> movimientos) {
        List<Long> varianteIds = movimientos.stream().map(m -> m.getVariante().getId()).distinct().toList();
        eventPublisher.publishEvent(new SaleRegisteredEvent(this, guardada.getId(), empresa.getId(), varianteIds));
    }

    @Transactional(readOnly = true)
    public SalesOrderResponseDTO getSalesOrderById(UserModel currentUser, Long id){
        SalesOrderModel orden = ordenVentaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden de venta con id " + id + " no encontrada"));

        if (!orden.getEmpresa().getId().equals(currentUser.getEmpresa().getId())) {
            throw new ResourceNotFoundException("Orden de venta con id " + id + " no encontrada");
        }

        return toDTO(orden);
    }

    private SalesOrderResponseDTO toDTO(SalesOrderModel orden) {
        List<SalesOrderLineResponseDTO> detalles = orden.getDetalles().stream()
                .map(d -> new SalesOrderLineResponseDTO(
                        d.getId(),
                        d.getVariante().getId(),
                        d.getVariante().getFullName(),
                        d.getCantidad(),
                        d.getPrecioUnitario(),
                        d.getSubtotal()
                ))
                .toList();

        return new SalesOrderResponseDTO(
                orden.getId(),
                orden.getEmpresa().getId(),
                orden.getCliente() != null ? orden.getCliente().getId() : null,
                orden.getCliente() != null ? orden.getCliente().getNombre() : null,
                orden.getFechaEmision(),
                orden.getEstado(),
                orden.getMedioPago(),
                orden.getCanal(),
                orden.getTotal(),
                detalles
        );
    }
}
