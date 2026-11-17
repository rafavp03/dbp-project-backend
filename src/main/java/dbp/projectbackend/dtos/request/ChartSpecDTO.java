package dbp.projectbackend.dtos.request;

import dbp.projectbackend.enums.ChartMetric;
import dbp.projectbackend.enums.ChartSource;
import dbp.projectbackend.enums.ChartType;
import dbp.projectbackend.enums.SalesGrouping;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Receta de un grafico: que datos mostrar y como. La puede escribir la IA a partir de una pregunta
 * o enviarla la app para volver a calcular un grafico guardado. Nunca contiene cifras.
 * Las fechas van como texto AAAA-MM-DD; si faltan se usan los ultimos 30 dias.
 */
public record ChartSpecDTO(
        @Size(max = 80, message = "El titulo no puede tener mas de 80 caracteres")
        String titulo,
        ChartType tipo,
        @NotNull(message = "La fuente de datos es obligatoria")
        ChartSource fuente,
        String desde,
        String hasta,
        SalesGrouping agrupacion,
        ChartMetric metrica,
        Integer limite
) {}
