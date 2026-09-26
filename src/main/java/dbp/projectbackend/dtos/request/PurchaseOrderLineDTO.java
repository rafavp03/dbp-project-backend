package dbp.projectbackend.dtos.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PurchaseOrderLineDTO(
        @NotNull(message = "Variante (talla y color) obligatoria")
        Long varianteId,

        @NotNull(message = "Cantidad obligatoria")
        @Min(value = 1, message = "Cantidad debe ser mayor a 0")
        Integer cantidad,

        @NotNull(message = "Precio unitario obligatorio")
        @DecimalMin(value = "0.00", inclusive = false, message = "Precio unitario debe ser mayor a 0")
        BigDecimal precioUnitario

) {}
