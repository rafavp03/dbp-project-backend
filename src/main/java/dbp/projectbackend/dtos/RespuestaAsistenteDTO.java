package dbp.projectbackend.dtos;

import java.time.LocalDateTime;

public record RespuestaAsistenteDTO(
        String respuesta,
        int consultasRestantesHoy,
        LocalDateTime fecha
) {}
