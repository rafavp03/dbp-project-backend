package dbp.projectbackend.dtos.request;

import dbp.projectbackend.enums.SalesChannel;
import dbp.projectbackend.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SalesOrderDTO(

        Long clienteId,

        @NotNull(message = "Medio de pago obligatorio")
        PaymentMethod medioPago,

        SalesChannel canal,

        @NotEmpty(message = "La orden debe tener al menos un detalle")
        @Valid
        List<SalesOrderLineDTO> detalles

){}
