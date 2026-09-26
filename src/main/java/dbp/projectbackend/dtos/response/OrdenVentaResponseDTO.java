package dbp.projectbackend.dtos.response;

import dbp.projectbackend.enums.CanalVenta;
import dbp.projectbackend.enums.EstadoOrden;
import dbp.projectbackend.enums.MedioPago;

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
