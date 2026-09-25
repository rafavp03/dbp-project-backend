package dbp.projectbackend.dtos;

import java.math.BigDecimal;

public record DetalleOrdenCompraResponseDTO(
        Long id,
        Long varianteId,
        String varianteDescripcion,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {}