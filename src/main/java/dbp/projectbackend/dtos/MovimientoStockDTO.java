package dbp.projectbackend.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// Para movimientos de ENTRADA, SALIDA y DEVOLUCION (siempre suman/restan una cantidad).
// El AJUSTE usa AjusteStockDTO porque fija el stock a un valor exacto, no suma/resta.
public record MovimientoStockDTO(
        @NotNull(message = "Variante obligatoria")
        Long varianteId,

        @NotNull(message = "Cantidad obligatoria")
        @Min(value = 1, message = "Cantidad debe ser mayor a 0")
        Integer cantidad,

        String motivo
) {}