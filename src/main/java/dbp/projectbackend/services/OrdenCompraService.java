package dbp.projectbackend.services;

import dbp.projectbackend.dtos.DetalleOrdenCompraDTO;
import dbp.projectbackend.dtos.DetalleOrdenCompraResponseDTO;
import dbp.projectbackend.dtos.OrdenCompraDTO;
import dbp.projectbackend.dtos.OrdenCompraResponseDTO;
import dbp.projectbackend.exceptions.ResourceNotFoundException;
import dbp.projectbackend.exceptions.ForbiddenException;
import dbp.projectbackend.models.*;
import dbp.projectbackend.repositories.MovimientoStockRepository;
import dbp.projectbackend.repositories.OrdenCompraRepository;
import dbp.projectbackend.repositories.SupplierRepository;
import dbp.projectbackend.repositories.VarianteProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class OrdenCompraService {

    private final OrdenCompraRepository ordenCompraRepository;
    private final SupplierRepository supplierRepository;
    private final VarianteProductoRepository varianteRepository;
    private final MovimientoStockRepository movimientoStockRepository;

    @Transactional
    public OrdenCompraResponseDTO createOrdenCompra(UserModel currentUser, OrdenCompraDTO dto) {
        EnterpriseModel empresa = currentUser.getEmpresa();

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

        return toDTO(guardada);
    }

    @Transactional(readOnly = true)
    public OrdenCompraResponseDTO getOrdenCompraById(UserModel currentUser, Long id) {
        OrdenCompraModel orden = ordenCompraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden de compra con id " + id + " no encontrada"));

        if (!orden.getEmpresa().getId().equals(currentUser.getEmpresa().getId())) {
            throw new ForbiddenException("No tienes acceso a esta orden de compra");
        }

        return toDTO(orden);
    }

    private OrdenCompraResponseDTO toDTO(OrdenCompraModel orden) {
        List<DetalleOrdenCompraResponseDTO> detalles = orden.getDetalles().stream()
                .map(d -> new DetalleOrdenCompraResponseDTO(
                        d.getId(),
                        d.getVariante().getId(),
                        d.getVariante().getNombreCompleto(),
                        d.getCantidad(),
                        d.getPrecioUnitario(),
                        d.getSubtotal()
                ))
                .toList();

        return new OrdenCompraResponseDTO(
                orden.getId(),
                orden.getEmpresa().getId(),
                orden.getProveedor().getId(),
                orden.getProveedor().getRazonSocial(),
                orden.getFechaEmision(),
                orden.getEstado(),
                orden.getTotal(),
                detalles
        );
    }
}