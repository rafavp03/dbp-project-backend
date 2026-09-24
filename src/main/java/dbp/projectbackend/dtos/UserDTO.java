package dbp.projectbackend.dtos;

import dbp.projectbackend.models.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// lo usa un ADMIN para crear usuarios dentro de su propia empresa
public record UserDTO(
        @NotBlank(message = "Nombre obligatorio")
        String nombre,

        @NotBlank(message = "Email obligatorio")
        @Email(message = "Email invalido")
        String email,

        @NotBlank(message = "Contraseña obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String password,

        @NotNull(message = "Rol obligatorio")
        Role role
) {}
