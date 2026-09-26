package dbp.projectbackend.dtos.response.report;

import java.math.BigDecimal;

public record ProductRankingDTO(
        Long productoId,
        String codigo,
        String producto,
        String categoria,
        long unidades,
        BigDecimal totalVendido,
        BigDecimal ganancia
) {}
