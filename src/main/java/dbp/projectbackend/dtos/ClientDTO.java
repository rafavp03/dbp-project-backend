package dbp.projectbackend.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ClientDTO(
        @NotBlank(message = "Documento obligatorio")
        @Pattern(regexp = "\\d{8}|\\d{11}", message = "Documento debe ser DNI (8 digitos) o RUC (11 digitos)")
        String documento,

        @NotBlank(message = "Nombre obligatorio")
        String nombre,

        Integer telefono,

        @Email(message = "Correo invalido")
        String correo,

        String direccion
) {}
