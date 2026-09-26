package dbp.projectbackend.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryDTO(
        @NotBlank(message = "Nombre obligatorio")
        String nombre,

        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
        String descripcion
) {}