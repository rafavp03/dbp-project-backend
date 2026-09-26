package dbp.projectbackend.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ClientDTO(
        @NotBlank(message = "Documento obligatorio")
        @Pattern(regexp = "\\d{8}|\\d{11}", message = "Documento debe ser DNI (8 digitos) o RUC (11 digitos)")
        String documento,

        @NotBlank(message = "Nombre obligatorio")
        String nombre,

        @Pattern(regexp = "\\+?\\d{6,15}", message = "Telefono debe tener entre 6 y 15 digitos, con + opcional")
        String telefono,

        @Email(message = "Correo invalido")
        String correo,

        String direccion
) {}
