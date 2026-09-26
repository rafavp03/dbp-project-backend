package dbp.projectbackend.dtos;

import java.time.LocalDateTime;

public record AssistantAnswerResponseDTO(
        String respuesta,
        int consultasRestantesHoy,
        LocalDateTime fecha
) {}
