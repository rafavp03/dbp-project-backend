package dbp.projectbackend.dtos.response;

import java.time.LocalDateTime;

public record AssistantAnswerResponseDTO(
        String respuesta,
        int consultasRestantesHoy,
        LocalDateTime fecha
) {}
