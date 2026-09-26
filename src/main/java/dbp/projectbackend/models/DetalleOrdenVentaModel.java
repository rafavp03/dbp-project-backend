package dbp.projectbackend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.math.BigDecimal;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
@Getter
@Entity
@Table(name = "detalle_orden_venta")

public class DetalleOrdenVentaModel {

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

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_venta_id", nullable = false)
    private OrdenVentaModel ordenVenta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variante_id", nullable = false)
    private VarianteProductoModel variante;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "precio_lista", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioLista;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        DetalleOrdenVentaModel other = (DetalleOrdenVentaModel) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
