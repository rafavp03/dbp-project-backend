package dbp.projectbackend.dtos.reportes;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResumenVentasDTO(
        LocalDate desde,
        LocalDate hasta,
        long numeroVentas,
        long unidadesVendidas,
        BigDecimal totalVendido,
        BigDecimal costoTotal,
        BigDecimal ganancia,
        BigDecimal margenPorcentaje,
        BigDecimal descuentoTotal,
        BigDecimal ticketPromedio
) {}
