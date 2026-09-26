package dbp.projectbackend.dtos.response;

import java.math.BigDecimal;

public record DetalleOrdenCompraResponseDTO(
        Long id,
        Long varianteId,
        String varianteDescripcion,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {}