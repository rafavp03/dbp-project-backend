package dbp.projectbackend.dtos.reportes;

import java.math.BigDecimal;
import java.time.LocalDate;

// Resumen de las ventas COMPLETADAS de un periodo
public record ResumenVentasDTO(
        LocalDate desde,
        LocalDate hasta,
        long numeroVentas,
        long unidadesVendidas,
        BigDecimal totalVendido,       // lo que se cobro
        BigDecimal costoTotal,         // lo que costo la mercaderia vendida
        BigDecimal ganancia,           // totalVendido - costoTotal
        BigDecimal margenPorcentaje,   // ganancia / totalVendido * 100
        BigDecimal descuentoTotal,     // cuanto se rebajo respecto al precio de lista
        BigDecimal ticketPromedio      // totalVendido / numeroVentas
) {}
