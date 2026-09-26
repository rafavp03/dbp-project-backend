package dbp.projectbackend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dbp.projectbackend.enums.SalesChannel;
import dbp.projectbackend.enums.OrderStatus;
import dbp.projectbackend.enums.PaymentMethod;
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
@Table(name = "ordenes_venta")

public class SalesOrderModel {

    public SalesOrderModel(EnterpriseModel empresa, ClientModel cliente, PaymentMethod medioPago, SalesChannel canal) {
        this.empresa = empresa;
        this.cliente = cliente;
        this.medioPago = medioPago;
        this.canal = canal;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    @JsonIgnoreProperties("empresa")
    private ClientModel cliente;

    @Column(name = "fecha_emision", nullable = false, updatable = false)
    private LocalDateTime fechaEmision;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "medio_pago", nullable = false)
    private PaymentMethod medioPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SalesChannel canal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @OneToMany(mappedBy = "ordenVenta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<SalesOrderLineModel> detalles = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.fechaEmision = LocalDateTime.now();
        if (this.estado == null) this.estado = OrderStatus.PENDIENTE;
        if (this.canal == null) this.canal = SalesChannel.TIENDA;
    }

    public void addLine(SalesOrderLineModel detalle) {
        detalles.add(detalle);
        detalle.setOrdenVenta(this);
    }

    public void removeLine(SalesOrderLineModel detalle) {
        detalles.remove(detalle);
        detalle.setOrdenVenta(null);
    }

    public void recalculateTotal() {
        this.total = detalles.stream()
                .map(SalesOrderLineModel::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalProfit() {
        return detalles.stream()
                .map(SalesOrderLineModel::getProfit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalDiscount() {
        return detalles.stream()
                .map(SalesOrderLineModel::getDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        SalesOrderModel other = (SalesOrderModel) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
