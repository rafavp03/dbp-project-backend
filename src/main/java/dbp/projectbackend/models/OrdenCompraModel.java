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
@Table(name = "ordenes_compra")

public class OrdenCompraModel {

    public OrdenCompraModel(EnterpriseModel empresa, SupplierModel proveedor) {
        this.empresa = empresa;
        this.proveedor = proveedor;
        this.estado = EstadoOrden.PENDIENTE;
        this.total = BigDecimal.ZERO;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Necesario porque un proveedor puede atender a varias empresas
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
    private EstadoOrden estado;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @OneToMany(mappedBy = "ordenCompra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleOrdenCompraModel> detalles = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.fechaEmision = LocalDateTime.now();
        if (this.estado == null) this.estado = EstadoOrden.PENDIENTE;
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
