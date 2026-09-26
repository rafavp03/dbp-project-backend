package dbp.projectbackend.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrdenCompraDTO(

        @NotNull(message = "Proveedor obligatorio")
        Long proveedorId,

        @NotEmpty(message = "La orden debe tener al menos un detalle")
        @Valid
        List<DetalleOrdenCompraDTO> detalles

) {}
