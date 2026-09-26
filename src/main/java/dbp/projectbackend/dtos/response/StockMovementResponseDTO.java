package dbp.projectbackend.dtos.response;

import dbp.projectbackend.enums.MovementType;

import java.time.LocalDateTime;

public record StockMovementResponseDTO(
        Long id,
        Long varianteId,
        String varianteDescripcion,
        MovementType tipo,
        Integer cantidad,
        Integer stockAnterior,
        Integer stockResultante,
        String motivo,
        LocalDateTime fecha
) {}
