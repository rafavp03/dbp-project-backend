package dbp.projectbackend.dtos.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductPatchDTO(
        @Size(min = 1, message = "El código no puede estar vacío")
        String codigo,

        @Size(min = 1, message = "El nombre no puede estar vacío")
        String nombre,

        String descripcion,

        @Size(min = 1, message = "La unidad de medida no puede estar vacía")
        String unidadMedida,

        @DecimalMin(value = "0.00", message = "El precio de compra no puede ser negativo")
        BigDecimal precioCompra,

        @DecimalMin(value = "0.00", inclusive = false, message = "El precio de venta debe ser mayor a 0")
        BigDecimal precioVenta,

        Long categoriaId
) {}
