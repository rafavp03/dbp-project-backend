package dbp.projectbackend.services;

import dbp.projectbackend.dtos.DetalleOrdenCompraDTO;
import dbp.projectbackend.dtos.OrdenCompraDTO;
import dbp.projectbackend.exceptions.ResourceNotFoundException;
import dbp.projectbackend.models.*;
import dbp.projectbackend.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service

public class OrdenCompraService {

    private final OrdenCompraRepository ordenCompraRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final SupplierRepository supplierRepository;
    private final VarianteProductoRepository varianteRepository;
    private final MovimientoStockRepository movimientoStockRepository;

    @Transactional
    public OrdenCompraModel createOrdenCompra(OrdenCompraDTO dto) {
        EnterpriseModel empresa = enterpriseRepository.findById(dto.empresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa con id " + dto.empresaId() + " no encontrada"));

        SupplierModel proveedor = supplierRepository.findById(dto.proveedorId())
                .filter(p -> p.getEmpresas().stream().anyMatch(e -> e.getId().equals(empresa.getId())))
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor con id " + dto.proveedorId() + " no encontrado"));

        OrdenCompraModel ordenCompra = new OrdenCompraModel(empresa, proveedor);
        List<MovimientoStockModel> movimientos = new ArrayList<>();

        for (DetalleOrdenCompraDTO detalleDto : dto.detalles()) {
            VarianteProductoModel variante = varianteRepository.findById(detalleDto.varianteId())
                    .filter(v -> v.getProducto().getEmpresa().getId().equals(empresa.getId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Variante con id " + detalleDto.varianteId() + " no encontrada"));

            ordenCompra.addDetalle(new DetalleOrdenCompraModel(variante, detalleDto.cantidad(), detalleDto.precioUnitario()));
            movimientos.add(MovimientoStockModel.entrada(variante, detalleDto.cantidad(), "Compra"));
        }

        ordenCompra.recalcularTotal();
        ordenCompra.setEstado(EstadoOrden.COMPLETADA);
        OrdenCompraModel guardada = ordenCompraRepository.save(ordenCompra);

        movimientos.forEach(m -> m.setOrdenCompra(guardada));
        movimientoStockRepository.saveAll(movimientos);

        return guardada;
    }

    public OrdenCompraModel getOrdenCompraById(Long id) {
        return ordenCompraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden de compra con id " + id + " no encontrada"));
    }
}