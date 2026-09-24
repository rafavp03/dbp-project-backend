package dbp.projectbackend.services;

import dbp.projectbackend.dtos.DetalleOrdenCompraDTO;
import dbp.projectbackend.dtos.OrdenCompraDTO;
import dbp.projectbackend.exceptions.ResourceNotFoundException;
import dbp.projectbackend.models.DetalleOrdenCompraModel;
import dbp.projectbackend.models.OrdenCompraModel;
import dbp.projectbackend.models.ProductModel;
import dbp.projectbackend.models.SupplierModel;
import dbp.projectbackend.repositories.OrdenCompraRepository;
import dbp.projectbackend.repositories.ProductRepository;
import dbp.projectbackend.repositories.SupplierRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service

public class OrdenCompraService {

    private final OrdenCompraRepository ordenCompraRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    @Transactional
    public OrdenCompraModel createOrdenCompra(OrdenCompraDTO dto) {
        SupplierModel proveedor = supplierRepository.findById(dto.proveedorId())
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor con id " + dto.proveedorId() + " no encontrado"));

        OrdenCompraModel ordenCompra = new OrdenCompraModel(proveedor);

        for (DetalleOrdenCompraDTO detalleDto : dto.detalles()) {
            ProductModel producto = productRepository.findById(detalleDto.productoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto con id " + detalleDto.productoId() + " no encontrado"));

            DetalleOrdenCompraModel detalle = new DetalleOrdenCompraModel(producto, detalleDto.cantidad(), detalleDto.precioUnitario());
            ordenCompra.addDetalle(detalle);
        }

        ordenCompra.recalcularTotal();
        return ordenCompraRepository.save(ordenCompra);
    }

    public OrdenCompraModel getOrdenCompraById(Long id) {
        return ordenCompraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden de compra con id " + id + " no encontrada"));
    }
}
