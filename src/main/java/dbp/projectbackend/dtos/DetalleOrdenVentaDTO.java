package dbp.projectbackend.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DetalleOrdenVentaDTO (
        @NotNull(message = "Producto Obligatorio")
        Long productoId,

        @NotNull(message = "Cantidad Obligatoria")
        @Min(value = 1, message = "Cantidad debe ser mayor a 0")
        Integer cantidad,

        @NotNull(message = "Precio Unitario Obligatorio")
        @DecimalMin(value = "0.00", inclusive = false, message = "Precio Unitario debe ser mayor a 0")
        BigDecimal precioUnitario

) {}

