package dbp.projectbackend.dtos.response.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SalesSummaryDTO(
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
