package dbp.projectbackend.services;

import dbp.projectbackend.dtos.DetalleOrdenVentaDTO;
import dbp.projectbackend.dtos.OrdenVentaDTO;
import dbp.projectbackend.exceptions.ResourceNotFoundException;
import dbp.projectbackend.models.DetalleOrdenVentaModel;
import dbp.projectbackend.models.OrdenVentaModel;
import dbp.projectbackend.repositories.OrdenVentaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service

public class OrdenVentaService {

    private final OrdenVentaRepository ordenVentaRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;

    @Transactional
    public OrdenVentaModel createOrdenVenta(OrdenVentaDTO dto){
        ClienteModel cliente = clientRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente con id " + dto.clienteId() + " no encontrado"));

        OrdenVentaModel ordenVenta = new OrdenVentaModel(cliente);

        for(DetalleOrdenVentaDTO detalleDto : dto.detalles()){
            ProductoModel producto = productRepository.findById(detalleDto.productoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto con id " + detalleDto.productoId() + " no encontrado"));

            DetalleOrdenVentaModel detalle = new DetalleOrdenVentaModel(producto, detalleDto.cantidad(), detalleDto.precioUnitario());
            ordenVenta.addDetalle(detalle);
        }

        ordenVenta.recalcularTotal();
        return ordenVentaRepository.save(ordenVenta);
    }

    public OrdenVentaModel getOrdenVentaById(Long id){
        return ordenVentaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden de venta con id " + id + " no encontrada"));
    }
}
