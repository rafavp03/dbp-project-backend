package dbp.projectbackend.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record VarianteProductoDTO(
        @NotBlank(message = "Talla obligatoria")
        String talla,

        @NotBlank(message = "Color obligatorio")
        String color,

        String sku,

        @Min(value = 0, message = "El stock mínimo no puede ser negativo")
        Integer stockMinimo
) {}