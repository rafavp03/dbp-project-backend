package dbp.projectbackend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PreguntaDTO(
        @NotBlank(message = "La pregunta es obligatoria")
        @Size(max = 500, message = "La pregunta no puede tener mas de 500 caracteres")
        String pregunta
) {}
