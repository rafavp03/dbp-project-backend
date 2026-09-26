package dbp.projectbackend.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MovimientoStockDTO(
        @NotNull(message = "Variante obligatoria")
        Long varianteId,

        @NotNull(message = "Cantidad obligatoria")
        @Min(value = 1, message = "Cantidad debe ser mayor a 0")
        Integer cantidad,

        String motivo
) {}