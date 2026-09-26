package dbp.projectbackend.dtos.response;

import java.math.BigDecimal;

public record SalesOrderLineResponseDTO(
        Long id,
        Long varianteId,
        String varianteDescripcion,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {}

