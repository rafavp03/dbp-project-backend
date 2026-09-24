package dbp.projectbackend.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// registro del primer usuario (ADMIN) de una empresa ya creada
public record RegisterDTO(
        @NotBlank(message = "Nombre obligatorio")
        String nombre,

        @NotBlank(message = "Email obligatorio")
        @Email(message = "Email invalido")
        String email,

        @NotBlank(message = "Contraseña obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String password,

        @NotNull(message = "Empresa obligatoria")
        Long enterpriseId
) {}
