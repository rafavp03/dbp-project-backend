package dbp.projectbackend.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

/**
 * Se publica cuando cambia el precio de venta o el costo de un producto (PUT o PATCH).
 * Los precios anteriores viajan en el evento porque después del commit ya no existen en la base.
 */
@Getter
public class ProductPriceChangedEvent extends ApplicationEvent {

    private final Long productoId;
    private final Long empresaId;
    private final String codigo;
    private final String nombre;
    private final BigDecimal precioVentaAnterior;
    private final BigDecimal precioVentaNuevo;
    private final BigDecimal precioCompraAnterior;
    private final BigDecimal precioCompraNuevo;
    private final String modificadoPor;

    public ProductPriceChangedEvent(Object source, Long productoId, Long empresaId, String codigo, String nombre,
                                    BigDecimal precioVentaAnterior, BigDecimal precioVentaNuevo,
                                    BigDecimal precioCompraAnterior, BigDecimal precioCompraNuevo,
                                    String modificadoPor) {
        super(source);
        this.productoId = productoId;
        this.empresaId = empresaId;
        this.codigo = codigo;
        this.nombre = nombre;
        this.precioVentaAnterior = precioVentaAnterior;
        this.precioVentaNuevo = precioVentaNuevo;
        this.precioCompraAnterior = precioCompraAnterior;
        this.precioCompraNuevo = precioCompraNuevo;
        this.modificadoPor = modificadoPor;
    }
}
