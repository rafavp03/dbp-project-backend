package dbp.projectbackend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// Kardex: cada cambio de stock de una variante (talla + color) queda registrado aqui.
// Los movimientos no se editan ni se eliminan; un error se corrige con otro movimiento.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity
@Table(name = "movimientos_stock")
public class MovimientoStockModel {

    private MovimientoStockModel(VarianteProductoModel variante, TipoMovimiento tipo, Integer cantidad,
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

    @ManyToOne(optional = false)
    @JoinColumn(name = "variante_id", nullable = false)
    private VarianteProductoModel variante;

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

    // factory methods: actualizan el stock de la variante y registran el movimiento

    public static MovimientoStockModel entrada(VarianteProductoModel variante, int cantidad, String motivo) {
        int anterior = variante.getStock();
        variante.aumentarStock(cantidad);
        return new MovimientoStockModel(variante, TipoMovimiento.ENTRADA, cantidad, anterior, variante.getStock(), motivo);
    }

    public static MovimientoStockModel salida(VarianteProductoModel variante, int cantidad, String motivo) {
        int anterior = variante.getStock();
        variante.disminuirStock(cantidad);
        return new MovimientoStockModel(variante, TipoMovimiento.SALIDA, cantidad, anterior, variante.getStock(), motivo);
    }

    public static MovimientoStockModel devolucion(VarianteProductoModel variante, int cantidad, String motivo) {
        int anterior = variante.getStock();
        variante.aumentarStock(cantidad);
        return new MovimientoStockModel(variante, TipoMovimiento.DEVOLUCION, cantidad, anterior, variante.getStock(), motivo);
    }

    public static MovimientoStockModel ajuste(VarianteProductoModel variante, int nuevoStock, String motivo) {
        int anterior = variante.getStock();
        if (nuevoStock == anterior) {
            throw new IllegalArgumentException("El nuevo stock es igual al actual, no hay nada que ajustar");
        }
        variante.ajustarStock(nuevoStock);
        return new MovimientoStockModel(variante, TipoMovimiento.AJUSTE, Math.abs(nuevoStock - anterior), anterior, nuevoStock, motivo);
    }

    // En el JSON solo se expone el id de la orden, no la orden completa
    public Long getOrdenCompraId() {
        return ordenCompra != null ? ordenCompra.getId() : null;
    }

    public Long getOrdenVentaId() {
        return ordenVenta != null ? ordenVenta.getId() : null;
    }
}
