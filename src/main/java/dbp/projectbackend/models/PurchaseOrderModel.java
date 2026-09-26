package dbp.projectbackend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dbp.projectbackend.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity
@Table(name = "ordenes_compra")

public class PurchaseOrderModel {

    public PurchaseOrderModel(EnterpriseModel empresa, SupplierModel proveedor) {
        this.empresa = empresa;
        this.proveedor = proveedor;
        this.estado = OrderStatus.PENDIENTE;
        this.total = BigDecimal.ZERO;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

   @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnoreProperties("proveedores")
    private EnterpriseModel empresa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proveedor_id", nullable = false)
    @JsonIgnoreProperties("empresas")
    private SupplierModel proveedor;

    @Column(name = "fecha_emision", nullable = false, updatable = false)
    private LocalDateTime fechaEmision;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus estado;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @OneToMany(mappedBy = "ordenCompra", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<PurchaseOrderLineModel> detalles = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.fechaEmision = LocalDateTime.now();
        if (this.estado == null) this.estado = OrderStatus.PENDIENTE;
    }

    public void addLine(PurchaseOrderLineModel detalle) {
        detalles.add(detalle);
        detalle.setOrdenCompra(this);
    }

    public void removeLine(PurchaseOrderLineModel detalle) {
        detalles.remove(detalle);
        detalle.setOrdenCompra(null);
    }

    public void recalculateTotal() {
        this.total = detalles.stream()
                .map(PurchaseOrderLineModel::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        PurchaseOrderModel other = (PurchaseOrderModel) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
