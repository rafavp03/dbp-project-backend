package dbp.projectbackend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
@Getter
@Entity
@Table(name = "detalle_orden_venta")

public class DetalleOrdenVentaModel {

    // precioLista y costoUnitario se copian del producto en el momento de la venta,
    // asi los reportes no cambian si despues sube el costo o el precio.
    public DetalleOrdenVentaModel(VarianteProductoModel variante, Integer cantidad, BigDecimal precioUnitario) {
        this.variante = variante;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;

        ProductModel producto = variante.getProducto();
        this.precioLista = producto.getPrecioVenta();
        this.costoUnitario = producto.getPrecioCompra() != null ? producto.getPrecioCompra() : BigDecimal.ZERO;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // evita recursion infinita al serializar la orden a JSON (orden -> detalles -> orden -> ...)
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "orden_venta_id", nullable = false)
    private OrdenVentaModel ordenVenta;

    @ManyToOne(optional = false)
    @JoinColumn(name = "variante_id", nullable = false)
    private VarianteProductoModel variante;

    @Column(nullable = false)
    private Integer cantidad;

    // Precio realmente cobrado (despues de rebaja / regateo)
    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    // Precio de lista al momento de la venta
    @Column(name = "precio_lista", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioLista;

    // Costo de compra al momento de la venta
    @Column(name = "costo_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal costoUnitario;

    public BigDecimal getSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public BigDecimal getGanancia() {
        return precioUnitario.subtract(costoUnitario).multiply(BigDecimal.valueOf(cantidad));
    }

    public BigDecimal getDescuento() {
        return precioLista.subtract(precioUnitario).max(BigDecimal.ZERO).multiply(BigDecimal.valueOf(cantidad));
    }
}
