package dbp.projectbackend.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginDTO(
        @NotBlank(message = "Email obligatorio")
        @Email(message = "Email invalido")
        String email,

        @NotBlank(message = "Contraseña obligatoria")
        String password
) {}
