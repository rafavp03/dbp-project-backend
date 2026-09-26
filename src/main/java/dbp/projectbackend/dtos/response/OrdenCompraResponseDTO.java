package dbp.projectbackend.dtos.response;

import dbp.projectbackend.enums.EstadoOrden;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrdenCompraResponseDTO(
        Long id,
        Long empresaId,
        Long proveedorId,
        String proveedorRazonSocial,
        LocalDateTime fechaEmision,
        EstadoOrden estado,
        BigDecimal total,
        List<DetalleOrdenCompraResponseDTO> detalles
) {}
