package dbp.projectbackend.services;

import dbp.projectbackend.dtos.DetalleOrdenVentaDTO;
import dbp.projectbackend.dtos.DetalleOrdenVentaResponseDTO;
import dbp.projectbackend.dtos.OrdenVentaDTO;
import dbp.projectbackend.dtos.OrdenVentaResponseDTO;
import dbp.projectbackend.exceptions.ResourceNotFoundException;
import dbp.projectbackend.models.*;
import dbp.projectbackend.repositories.ClientRepository;
import dbp.projectbackend.repositories.MovimientoStockRepository;
import dbp.projectbackend.repositories.OrdenVentaRepository;
import dbp.projectbackend.repositories.VarianteProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class OrdenVentaService {

    private final OrdenVentaRepository ordenVentaRepository;
    private final ClientRepository clientRepository;
    private final VarianteProductoRepository varianteRepository;
    private final MovimientoStockRepository movimientoStockRepository;

    @Transactional
    public OrdenVentaResponseDTO createOrdenVenta(UserModel currentUser, OrdenVentaDTO dto){
        EnterpriseModel empresa = currentUser.getEmpresa();

        ClientModel cliente = null;
        if (dto.clienteId() != null) {
            cliente = clientRepository.findById(dto.clienteId())
                    .filter(c -> c.getEmpresa().getId().equals(empresa.getId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente con id " + dto.clienteId() + " no encontrado"));
        }

        OrdenVentaModel ordenVenta = new OrdenVentaModel(empresa, cliente, dto.medioPago(), dto.canal());
        List<MovimientoStockModel> movimientos = new ArrayList<>();

        for(DetalleOrdenVentaDTO detalleDto : dto.detalles()){
            VarianteProductoModel variante = varianteRepository.findById(detalleDto.varianteId())
                    .filter(v -> v.getProducto().getEmpresa().getId().equals(empresa.getId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Variante con id " + detalleDto.varianteId() + " no encontrada"));

            BigDecimal precio = detalleDto.precioUnitario() != null
                    ? detalleDto.precioUnitario()
                    : variante.getProducto().getPrecioVenta();

            ordenVenta.addDetalle(new DetalleOrdenVentaModel(variante, detalleDto.cantidad(), precio));
            movimientos.add(MovimientoStockModel.salida(variante, detalleDto.cantidad(), "Venta"));
        }

        ordenVenta.recalcularTotal();
        ordenVenta.setEstado(EstadoOrden.COMPLETADA);
        OrdenVentaModel guardada = ordenVentaRepository.save(ordenVenta);

        movimientos.forEach(m -> m.setOrdenVenta(guardada));
        movimientoStockRepository.saveAll(movimientos);

        return toDTO(guardada);
    }

    @Transactional(readOnly = true)
    public OrdenVentaResponseDTO getOrdenVentaById(UserModel currentUser, Long id){
        OrdenVentaModel orden = ordenVentaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden de venta con id " + id + " no encontrada"));

        if (!orden.getEmpresa().getId().equals(currentUser.getEmpresa().getId())) {
            throw new ResourceNotFoundException("Orden de venta con id " + id + " no encontrada");
        }

        return toDTO(orden);
    }

    private OrdenVentaResponseDTO toDTO(OrdenVentaModel orden) {
        List<DetalleOrdenVentaResponseDTO> detalles = orden.getDetalles().stream()
                .map(d -> new DetalleOrdenVentaResponseDTO(
                        d.getId(),
                        d.getVariante().getId(),
                        d.getVariante().getNombreCompleto(),
                        d.getCantidad(),
                        d.getPrecioUnitario(),
                        d.getSubtotal()
                ))
                .toList();

        return new OrdenVentaResponseDTO(
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