package dbp.projectbackend.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AjusteStockDTO(
        @NotNull(message = "Variante obligatoria")
        Long varianteId,

        @NotNull(message = "Nuevo stock obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer nuevoStock,

        String motivo
) {}