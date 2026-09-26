package dbp.projectbackend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dbp.projectbackend.exceptions.InsufficientStockException;
import dbp.projectbackend.exceptions.InvalidOperationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity
@Table(
    name = "variantes_producto",
    uniqueConstraints = @UniqueConstraint(columnNames = {"producto_id", "talla", "color"})
)
public class ProductVariantModel {

    public ProductVariantModel(String talla, String color) {
        this.talla = talla;
        this.color = color;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    @JsonIgnoreProperties({"variantes", "empresa"})
    private ProductModel producto;

    @Column(nullable = false)
    private String talla;

    @Column(nullable = false)
    private String color;

    private String sku;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo = 0;

    @Column(nullable = false)
    private Boolean activo = true;

    @PrePersist
    protected void onCreate() {
        if (this.stock == null) this.stock = 0;
        if (this.stockMinimo == null) this.stockMinimo = 0;
        if (this.activo == null) this.activo = true;
    }

    public void increaseStock(int cantidad) {
        if (cantidad <= 0) {
            throw new InvalidOperationException("La cantidad debe ser mayor a 0");
        }
        this.stock += cantidad;
    }

    public void decreaseStock(int cantidad) {
        if (cantidad <= 0) {
            throw new InvalidOperationException("La cantidad debe ser mayor a 0");
        }
        if (this.stock < cantidad) {
            throw new InsufficientStockException(
                "Stock insuficiente para " + getFullName()
                    + " (disponible: " + stock + ", solicitado: " + cantidad + ")"
            );
        }
        this.stock -= cantidad;
    }

    public void adjustStock(int nuevoStock) {
        if (nuevoStock < 0) {
            throw new InvalidOperationException("El stock no puede ser negativo");
        }
        this.stock = nuevoStock;
    }

    public boolean isLowStock() {
        return stock <= stockMinimo;
    }

    public String getFullName() {
        String nombre = producto != null ? producto.getNombre() : "";
        return nombre + " - " + talla + " - " + color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ProductVariantModel other = (ProductVariantModel) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
