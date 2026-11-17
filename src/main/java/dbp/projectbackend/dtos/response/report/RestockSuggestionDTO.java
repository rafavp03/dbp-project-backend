package dbp.projectbackend.dtos.response.report;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Sugerencia de reposicion de una talla y color, calculada con el ritmo de venta real del negocio.
 * urgencia: AGOTADO (sin stock y con demanda), CRITICO (se agota en 7 dias o menos),
 * PRONTO (en 14 dias o menos) o PLANIFICAR (dentro del periodo de cobertura o bajo su stock minimo).
 */
public record RestockSuggestionDTO(
        Long varianteId,
        Long productoId,
        String codigo,
        String producto,
        String talla,
        String color,
        int stock,
        int stockMinimo,
        int unidadesVendidas,
        BigDecimal ventaDiaria,
        Integer diasRestantes,
        LocalDate fechaAgotamiento,
        String urgencia,
        int cantidadSugerida,
        BigDecimal costoUnitario,
        BigDecimal costoEstimado,
        BigDecimal ventaEnRiesgoDiaria,
        Long proveedorId,
        String proveedorRazonSocial
) {}
