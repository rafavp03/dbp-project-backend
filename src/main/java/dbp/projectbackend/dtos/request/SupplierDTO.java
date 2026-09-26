package dbp.projectbackend.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SupplierDTO(
        @NotBlank(message = "RUC obligatorio")
        @Size(min = 11, max = 11, message = "RUC debe tener 11 caracteres")
        @Pattern(regexp = "\\d{11}", message = "RUC debe tener 11 digitos")
        String ruc,

        @NotBlank(message = "Razón social obligatoria")
        String razonSocial,

        @Pattern(regexp = "\\+?\\d{6,15}", message = "Telefono debe tener entre 6 y 15 digitos, con + opcional")
        String telefono,

        @Email(message = "Correo invalido")
        String correo
) {}
