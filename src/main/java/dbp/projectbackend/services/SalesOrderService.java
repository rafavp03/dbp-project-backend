package dbp.projectbackend.services;

import dbp.projectbackend.dtos.request.SalesOrderLineDTO;
import dbp.projectbackend.dtos.request.SalesOrderDTO;
import dbp.projectbackend.dtos.response.PageResponseDTO;
import dbp.projectbackend.dtos.response.SalesOrderResponseDTO;
import dbp.projectbackend.enums.OrderStatus;
import dbp.projectbackend.events.SaleRegisteredEvent;
import dbp.projectbackend.exceptions.ResourceNotFoundException;
import dbp.projectbackend.mappers.SalesOrderMapper;
import dbp.projectbackend.models.*;
import dbp.projectbackend.repositories.ClientRepository;
import dbp.projectbackend.repositories.StockMovementRepository;
import dbp.projectbackend.repositories.SalesOrderRepository;
import dbp.projectbackend.repositories.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private final SalesOrderMapper salesOrderMapper;

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
        return salesOrderMapper.toResponse(guardada);
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
        List<SaleRegisteredEvent.SaleLine> lineas = guardada.getDetalles().stream()
                .map(d -> new SaleRegisteredEvent.SaleLine(
                        d.getVariante().getFullName(), d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal()))
                .toList();
        ClientModel cliente = guardada.getCliente();

        eventPublisher.publishEvent(new SaleRegisteredEvent(
                this,
                guardada.getId(),
                empresa.getId(),
                empresa.getRazonSocial(),
                varianteIds,
                cliente != null ? cliente.getNombre() : null,
                cliente != null ? cliente.getCorreo() : null,
                guardada.getFechaEmision(),
                guardada.getMedioPago(),
                guardada.getTotal(),
                lineas));
    }

    /**
     * Lista las ventas de la empresa, por defecto las más recientes primero.
     * Si se envía {@code desde} y/o {@code hasta}, filtra por fecha de emisión (ambos inclusive).
     */
    @Transactional(readOnly = true)
    public PageResponseDTO<SalesOrderResponseDTO> getSalesOrders(UserModel currentUser, LocalDate desde,
                                                                 LocalDate hasta, Pageable pageable) {
        Long empresaId = currentUser.getEmpresa().getId();
        Page<SalesOrderModel> ventas;
        if (desde == null && hasta == null) {
            ventas = ordenVentaRepository.findByEmpresaId(empresaId, pageable);
        } else {
            DateRange rango = DateRange.of(desde, hasta);
            ventas = ordenVentaRepository.findByEmpresaIdAndFechaEmisionGreaterThanEqualAndFechaEmisionLessThan(
                    empresaId, rango.inicio(), rango.finExclusivo(), pageable);
        }
        return PageResponseDTO.from(ventas.map(salesOrderMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public SalesOrderResponseDTO getSalesOrderById(UserModel currentUser, Long id){
        SalesOrderModel orden = ordenVentaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden de venta con id " + id + " no encontrada"));

        if (!orden.getEmpresa().getId().equals(currentUser.getEmpresa().getId())) {
            throw new ResourceNotFoundException("Orden de venta con id " + id + " no encontrada");
        }

        return salesOrderMapper.toResponse(orden);
    }
}
