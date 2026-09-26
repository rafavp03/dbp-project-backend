package dbp.projectbackend.dtos.response;

import dbp.projectbackend.enums.TipoMovimiento;

import java.time.LocalDateTime;

public record MovimientoStockResponseDTO(
        Long id,
        Long varianteId,
        String varianteDescripcion,
        TipoMovimiento tipo,
        Integer cantidad,
        Integer stockAnterior,
        Integer stockResultante,
        String motivo,
        LocalDateTime fecha
) {}
