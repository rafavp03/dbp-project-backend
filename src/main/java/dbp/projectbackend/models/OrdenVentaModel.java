package dbp.projectbackend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity
@Table(name = "ordenes_venta")

public class OrdenVentaModel {

    public OrdenVentaModel(EnterpriseModel empresa, ClientModel cliente, MedioPago medioPago, CanalVenta canal) {
        this.empresa = empresa;
        this.cliente = cliente;
        this.medioPago = medioPago;
        this.canal = canal;
        this.estado = EstadoOrden.PENDIENTE;
        this.total = BigDecimal.ZERO;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnoreProperties("proveedores")
    private EnterpriseModel empresa;

    // Opcional: en una tienda minorista la mayoria compra de paso y no deja sus datos
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    @JsonIgnoreProperties("empresa")
    private ClientModel cliente;

    // Con hora, para analizar en que dias y horas se vende mas
    @Column(name = "fecha_emision", nullable = false, updatable = false)
    private LocalDateTime fechaEmision;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoOrden estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "medio_pago", nullable = false)
    private MedioPago medioPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CanalVenta canal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @OneToMany(mappedBy = "ordenVenta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleOrdenVentaModel> detalles = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.fechaEmision = LocalDateTime.now();
        if (this.estado == null) this.estado = EstadoOrden.PENDIENTE;
        if (this.canal == null) this.canal = CanalVenta.TIENDA;
    }

    public void addDetalle(DetalleOrdenVentaModel detalle) {
        detalles.add(detalle);
        detalle.setOrdenVenta(this);
    }

    public void removeDetalle(DetalleOrdenVentaModel detalle) {
        detalles.remove(detalle);
        detalle.setOrdenVenta(null);
    }

    public void recalcularTotal() {
        this.total = detalles.stream()
                .map(DetalleOrdenVentaModel::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Ganancia de toda la venta (precio cobrado - costo)
    public BigDecimal getGananciaTotal() {
        return detalles.stream()
                .map(DetalleOrdenVentaModel::getGanancia)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Cuanto se rebajo en total respecto al precio de lista
    public BigDecimal getDescuentoTotal() {
        return detalles.stream()
                .map(DetalleOrdenVentaModel::getDescuento)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
