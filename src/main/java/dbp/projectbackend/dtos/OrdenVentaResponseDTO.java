package dbp.projectbackend.dtos;

import dbp.projectbackend.models.CanalVenta;
import dbp.projectbackend.models.EstadoOrden;
import dbp.projectbackend.models.MedioPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrdenVentaResponseDTO(
        Long id,
        Long empresaId,
        Long clienteId,
        String clienteNombre,
        LocalDateTime fechaEmision,
        EstadoOrden estado,
        MedioPago medioPago,
        CanalVenta canal,
        BigDecimal total,
        List<DetalleOrdenVentaResponseDTO> detalles
) {}