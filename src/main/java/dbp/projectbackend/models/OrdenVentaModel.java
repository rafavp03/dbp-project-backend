package dbp.projectbackend.models;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity
@Table(name = "ordenes_venta")

public class OrdenVentaModel {

    public OrdenVentaModel(ClientModel cliente) {
        this.cliente = cliente;
        this.estado = "PENDIENTE";
        this.total = BigDecimal.ZERO;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(Optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private ClientModel cliente;

    @Column(name = "fecha_venta", nullable = false, updatable = false)
    private LocalDate fechaVenta;

    @Column(nullable = false)
    private String estado;

    @Column(nullable = false)
    private BigDecimal total;

    @OneToMany(mappedBy = "ordenVenta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleOrdenVentaModel> detalles = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.fechaVenta = LocalDate.now();
    }

    public void addDetalle(DetalleOrdenVentaModel detalle) {
        detalle.add(detalle);
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
}
