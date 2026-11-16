package dbp.projectbackend.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record VisualDTO(
        String tipo,
        String titulo,
        String formato,
        List<VisualPointDTO> datos,
        List<VisualColumnDTO> columnas,
        List<Map<String, Object>> filas
) {}
