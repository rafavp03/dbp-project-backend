package dbp.projectbackend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnterpriseDTO(
        @NotBlank(message = "RUC obligatorio")
        @Size(min = 11, max = 11, message = "RUC debe tener 11 caracteres")
        @Pattern(regexp = "\\d{11}", message = "RUC debe tener 11 digitos")
        String ruc,

        @NotBlank(message = "Razón social obligatoria")
        String razonSocial
) {}
