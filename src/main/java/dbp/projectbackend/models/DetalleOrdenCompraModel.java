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
@Table(name = "detalle_orden_compra")

public class DetalleOrdenCompraModel {

    public DetalleOrdenCompraModel(VarianteProductoModel variante, Integer cantidad, BigDecimal precioUnitario) {
        this.variante = variante;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // evita recursion infinita al serializar la orden a JSON (orden -> detalles -> orden -> ...)
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "orden_compra_id", nullable = false)
    private OrdenCompraModel ordenCompra;

    @ManyToOne(optional = false)
    @JoinColumn(name = "variante_id", nullable = false)
    private VarianteProductoModel variante;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    public BigDecimal getSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
}
