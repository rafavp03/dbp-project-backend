package dbp.projectbackend.events;

import dbp.projectbackend.enums.PaymentMethod;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Se publica al completar una venta. Lleva copias de los datos (no entidades) porque los
 * listeners corren en otro hilo, después del commit, sin sesión de Hibernate abierta.
 */
@Getter
public class SaleRegisteredEvent extends ApplicationEvent {

    public record SaleLine(String descripcion, int cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {}

    private final Long ventaId;
    private final Long empresaId;
    private final String empresa;
    private final List<Long> varianteIds;
    private final String clienteNombre;
    private final String clienteCorreo;
    private final LocalDateTime fecha;
    private final PaymentMethod medioPago;
    private final BigDecimal total;
    private final List<SaleLine> lineas;

    public SaleRegisteredEvent(Object source, Long ventaId, Long empresaId, String empresa, List<Long> varianteIds,
                               String clienteNombre, String clienteCorreo, LocalDateTime fecha,
                               PaymentMethod medioPago, BigDecimal total, List<SaleLine> lineas) {
        super(source);
        this.ventaId = ventaId;
        this.empresaId = empresaId;
        this.empresa = empresa;
        this.varianteIds = List.copyOf(varianteIds);
        this.clienteNombre = clienteNombre;
        this.clienteCorreo = clienteCorreo;
        this.fecha = fecha;
        this.medioPago = medioPago;
        this.total = total;
        this.lineas = List.copyOf(lineas);
    }

    public boolean hasClientEmail() {
        return clienteCorreo != null && !clienteCorreo.isBlank();
    }
}
