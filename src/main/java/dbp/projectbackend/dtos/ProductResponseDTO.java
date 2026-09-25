package dbp.projectbackend.dtos;

import java.math.BigDecimal;

public record ProductResponseDTO(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        String unidadMedida,
        BigDecimal precioCompra,
        BigDecimal precioVenta,
        Boolean activo,
        Long categoriaId,
        String categoriaNombre,
        Long empresaId,
        Integer stockTotal
) {}