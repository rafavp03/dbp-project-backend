package dbp.projectbackend.dtos.reportes;

import java.math.BigDecimal;

public record VentasAgrupadasDTO(
        String grupo,
        long numeroVentas,
        long unidades,
        BigDecimal total,
        BigDecimal porcentajeDelTotal
) {}
