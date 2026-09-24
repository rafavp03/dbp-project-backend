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
@Table(name = "ordenes_compra")

public class OrdenCompraModel {

    public OrdenCompraModel(SupplierModel proveedor) {
        this.proveedor = proveedor;
        this.estado = "PENDIENTE";
        this.total = BigDecimal.ZERO;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private SupplierModel proveedor;

    @Column(name = "fecha_emision", nullable = false, updatable = false)
    private LocalDate fechaEmision;

    @Column(nullable = false)
    private String estado;

    @Column(nullable = false)
    private BigDecimal total;

    @OneToMany(mappedBy = "ordenCompra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleOrdenCompraModel> detalles = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.fechaEmision = LocalDate.now();
    }

    public void addDetalle(DetalleOrdenCompraModel detalle) {
        detalles.add(detalle);
        detalle.setOrdenCompra(this);
    }

    public void removeDetalle(DetalleOrdenCompraModel detalle) {
        detalles.remove(detalle);
        detalle.setOrdenCompra(null);
    }

    public void recalcularTotal() {
        this.total = detalles.stream()
                .map(DetalleOrdenCompraModel::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
