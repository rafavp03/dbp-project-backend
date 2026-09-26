package dbp.projectbackend.dtos.response;

import java.time.LocalDateTime;

public record ConsultaIAResponseDTO(
        Long id,
        String pregunta,
        String respuesta,
        boolean exitosa,
        LocalDateTime fecha
) {}
