package dbp.projectbackend.dtos.reportes;

import java.math.BigDecimal;

public record ProductoRankingDTO(
        Long productoId,
        String codigo,
        String producto,
        String categoria,
        long unidades,
        BigDecimal totalVendido,
        BigDecimal ganancia
) {}
