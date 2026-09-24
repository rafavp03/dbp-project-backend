package dbp.projectbackend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// Kardex: cada cambio de stock de un producto queda registrado aqui.
// Los movimientos no se editan ni se eliminan; un error se corrige con otro movimiento.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity
@Table(name = "movimientos_stock")
public class MovimientoStockModel {

    private MovimientoStockModel(ProductModel producto, TipoMovimiento tipo, Integer cantidad,
                                 Integer stockAnterior, Integer stockResultante, String motivo) {
        this.producto = producto;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.stockAnterior = stockAnterior;
        this.stockResultante = stockResultante;
        this.motivo = motivo;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    @JsonIgnoreProperties({"empresa", "categoria"})
    private ProductModel producto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimiento tipo;

    // Unidades movidas, siempre positivo. La direccion la indica el tipo
    // (en un AJUSTE se ve comparando stockAnterior con stockResultante).
    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "stock_anterior", nullable = false)
    private Integer stockAnterior;

    @Column(name = "stock_resultante", nullable = false)
    private Integer stockResultante;

    private String motivo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    // Documento que origino el movimiento (opcional: un ajuste no tiene orden)
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "orden_compra_id")
    private OrdenCompraModel ordenCompra;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "orden_venta_id")
    private OrdenVentaModel ordenVenta;

    // lifecycle methods
    @PrePersist
    protected void onCreate() {
        this.fecha = LocalDateTime.now();
    }

    // factory methods: actualizan el stock del producto y registran el movimiento

    public static MovimientoStockModel entrada(ProductModel producto, int cantidad, String motivo) {
        int anterior = producto.getStock();
        producto.aumentarStock(cantidad);
        return new MovimientoStockModel(producto, TipoMovimiento.ENTRADA, cantidad, anterior, producto.getStock(), motivo);
    }

    public static MovimientoStockModel salida(ProductModel producto, int cantidad, String motivo) {
        int anterior = producto.getStock();
        producto.disminuirStock(cantidad);
        return new MovimientoStockModel(producto, TipoMovimiento.SALIDA, cantidad, anterior, producto.getStock(), motivo);
    }

    public static MovimientoStockModel ajuste(ProductModel producto, int nuevoStock, String motivo) {
        int anterior = producto.getStock();
        if (nuevoStock == anterior) {
            throw new IllegalArgumentException("El nuevo stock es igual al actual, no hay nada que ajustar");
        }
        producto.ajustarStock(nuevoStock);
        return new MovimientoStockModel(producto, TipoMovimiento.AJUSTE, Math.abs(nuevoStock - anterior), anterior, nuevoStock, motivo);
    }

    // En el JSON solo se expone el id de la orden, no la orden completa
    public Long getOrdenCompraId() {
        return ordenCompra != null ? ordenCompra.getId() : null;
    }

    public Long getOrdenVentaId() {
        return ordenVenta != null ? ordenVenta.getId() : null;
    }
}
