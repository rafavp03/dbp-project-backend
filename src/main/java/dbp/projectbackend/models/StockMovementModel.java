package dbp.projectbackend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dbp.projectbackend.enums.MovementType;
import dbp.projectbackend.exceptions.InvalidOperationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity
@Table(
    name = "movimientos_stock",
    indexes = @Index(name = "idx_movimientos_stock_variante_fecha", columnList = "variante_id, fecha")
)
public class StockMovementModel {

    private StockMovementModel(ProductVariantModel variante, MovementType tipo, Integer cantidad,
                                 Integer stockAnterior, Integer stockResultante, String motivo) {
        this.variante = variante;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.stockAnterior = stockAnterior;
        this.stockResultante = stockResultante;
        this.motivo = motivo;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variante_id", nullable = false)
    private ProductVariantModel variante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType tipo;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "stock_anterior", nullable = false)
    private Integer stockAnterior;

    @Column(name = "stock_resultante", nullable = false)
    private Integer stockResultante;

    private String motivo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_compra_id")
    private PurchaseOrderModel ordenCompra;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_venta_id")
    private SalesOrderModel ordenVenta;

    @PrePersist
    protected void onCreate() {
        this.fecha = LocalDateTime.now();
    }

    public static StockMovementModel inbound(ProductVariantModel variante, int cantidad, String motivo) {
        int anterior = variante.getStock();
        variante.increaseStock(cantidad);
        return new StockMovementModel(variante, MovementType.ENTRADA, cantidad, anterior, variante.getStock(), motivo);
    }

    public static StockMovementModel outbound(ProductVariantModel variante, int cantidad, String motivo) {
        int anterior = variante.getStock();
        variante.decreaseStock(cantidad);
        return new StockMovementModel(variante, MovementType.SALIDA, cantidad, anterior, variante.getStock(), motivo);
    }

    public static StockMovementModel customerReturn(ProductVariantModel variante, int cantidad, String motivo) {
        int anterior = variante.getStock();
        variante.increaseStock(cantidad);
        return new StockMovementModel(variante, MovementType.DEVOLUCION, cantidad, anterior, variante.getStock(), motivo);
    }

    public static StockMovementModel adjustment(ProductVariantModel variante, int nuevoStock, String motivo) {
        int anterior = variante.getStock();
        if (nuevoStock == anterior) {
            throw new InvalidOperationException("El nuevo stock es igual al actual, no hay nada que ajustar");
        }
        variante.adjustStock(nuevoStock);
        return new StockMovementModel(variante, MovementType.AJUSTE, Math.abs(nuevoStock - anterior), anterior, nuevoStock, motivo);
    }

    public Long getOrdenCompraId() {
        return ordenCompra != null ? ordenCompra.getId() : null;
    }

    public Long getOrdenVentaId() {
        return ordenVenta != null ? ordenVenta.getId() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        StockMovementModel other = (StockMovementModel) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
