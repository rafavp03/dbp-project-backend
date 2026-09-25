package dbp.projectbackend.dtos.reportes;

import java.math.BigDecimal;

// Una fila de un reporte agrupado: por medio de pago, canal, categoria, dia o hora
public record VentasAgrupadasDTO(
        String grupo,                  // ej. "YAPE", "TIENDA", "Polos", "SABADO", "18:00-19:00"
        long numeroVentas,
        long unidades,
        BigDecimal total,
        BigDecimal porcentajeDelTotal
) {}
