package dbp.projectbackend.dtos.response;

import dbp.projectbackend.dtos.request.ChartSpecDTO;

import java.util.List;

/**
 * Grafico listo para dibujar. spec es la receta ya normalizada (la app la guarda para refrescarlo),
 * unidad es SOLES o UNIDADES o VENTAS, y preguntasRestantes solo viene cuando lo genero la IA.
 */
public record ChartResponseDTO(
        ChartSpecDTO spec,
        String unidad,
        List<ChartPointDTO> puntos,
        String resumen,
        Integer preguntasRestantes
) {}
