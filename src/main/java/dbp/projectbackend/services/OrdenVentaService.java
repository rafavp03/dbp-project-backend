package dbp.projectbackend.services;

import dbp.projectbackend.dtos.DetalleOrdenVentaDTO;
import dbp.projectbackend.dtos.OrdenVentaDTO;
import dbp.projectbackend.exceptions.ResourceNotFoundException;
import dbp.projectbackend.models.*;
import dbp.projectbackend.repositories.ClientRepository;
import dbp.projectbackend.repositories.EnterpriseRepository;
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
    private final EnterpriseRepository enterpriseRepository;
    private final ClientRepository clientRepository;
    private final VarianteProductoRepository varianteRepository;
    private final MovimientoStockRepository movimientoStockRepository;

    // Venta minorista: se entrega en el momento, asi que nace COMPLETADA y descuenta stock.
    // Si falta stock de alguna variante, se lanza 409 y @Transactional deshace toda la venta.
    @Transactional
    public OrdenVentaModel createOrdenVenta(OrdenVentaDTO dto){
        EnterpriseModel empresa = enterpriseRepository.findById(dto.empresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa con id " + dto.empresaId() + " no encontrada"));

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

        return guardada;
    }

    public OrdenVentaModel getOrdenVentaById(Long id){
        return ordenVentaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden de venta con id " + id + " no encontrada"));
    }
}
