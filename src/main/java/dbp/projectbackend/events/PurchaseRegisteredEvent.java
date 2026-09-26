package dbp.projectbackend.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PurchaseRegisteredEvent extends ApplicationEvent {

    public record PurchaseLine(String descripcion, int cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {}

    private final Long compraId;
    private final Long empresaId;
    private final String proveedor;
    private final LocalDateTime fecha;
    private final BigDecimal total;
    private final List<PurchaseLine> lineas;

    public PurchaseRegisteredEvent(Object source, Long compraId, Long empresaId, String proveedor,
                                 LocalDateTime fecha, BigDecimal total, List<PurchaseLine> lineas) {
        super(source);
        this.compraId = compraId;
        this.empresaId = empresaId;
        this.proveedor = proveedor;
        this.fecha = fecha;
        this.total = total;
        this.lineas = List.copyOf(lineas);
    }
}
