package dbp.projectbackend.dtos;

import dbp.projectbackend.models.TipoMovimiento;

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