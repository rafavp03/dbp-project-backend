package dbp.projectbackend.dtos.response;

import java.time.LocalDateTime;

public record AiQueryResponseDTO(
        Long id,
        String pregunta,
        String respuesta,
        boolean exitosa,
        LocalDateTime fecha
) {}
