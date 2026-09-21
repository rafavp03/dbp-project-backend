package dbp.projectbackend.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrdenVentaDTO(

        @NotNull(message = "Cliente Obligatorio")
        Long clienteId,

        @NotEmpty(message = "La orden debe tener al menos un detalle")
        @Valid
        List<DetalleOrdenVentaDTO> detalles

){}

