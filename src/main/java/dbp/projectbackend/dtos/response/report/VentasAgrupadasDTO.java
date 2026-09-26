package dbp.projectbackend.dtos.response.report;

import java.math.BigDecimal;

public record VentasAgrupadasDTO(
        String grupo,
        long numeroVentas,
        long unidades,
        BigDecimal total,
        BigDecimal porcentajeDelTotal
) {}
