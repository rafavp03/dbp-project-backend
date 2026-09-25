package dbp.projectbackend.dtos;

import dbp.projectbackend.models.CanalVenta;
import dbp.projectbackend.models.MedioPago;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrdenVentaDTO(

        Long clienteId,

        @NotNull(message = "Medio de pago obligatorio")
        MedioPago medioPago,

        CanalVenta canal,

        @NotEmpty(message = "La orden debe tener al menos un detalle")
        @Valid
        List<DetalleOrdenVentaDTO> detalles

){}
