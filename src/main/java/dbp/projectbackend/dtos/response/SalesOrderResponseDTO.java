package dbp.projectbackend.dtos.response;

import dbp.projectbackend.enums.SalesChannel;
import dbp.projectbackend.enums.OrderStatus;
import dbp.projectbackend.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SalesOrderResponseDTO(
        Long id,
        Long empresaId,
        Long clienteId,
        String clienteNombre,
        LocalDateTime fechaEmision,
        OrderStatus estado,
        PaymentMethod medioPago,
        SalesChannel canal,
        BigDecimal total,
        List<SalesOrderLineResponseDTO> detalles
) {}
