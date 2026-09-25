package dbp.projectbackend.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductDTO(
        @NotBlank(message = "Código obligatorio")
        String codigo,

        @NotBlank(message = "Nombre obligatorio")
        String nombre,

        String descripcion,

        String unidadMedida,

        @DecimalMin(value = "0.00", message = "El precio de compra no puede ser negativo")
        BigDecimal precioCompra,

        @NotNull(message = "Precio de venta obligatorio")
        @DecimalMin(value = "0.00", inclusive = false, message = "El precio de venta debe ser mayor a 0")
        BigDecimal precioVenta,

        Long categoriaId
) {}