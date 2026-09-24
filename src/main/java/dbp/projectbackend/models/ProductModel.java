package dbp.projectbackend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dbp.projectbackend.exceptions.DataIntegrityViolationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity
@Table(
    name = "productos",
    uniqueConstraints = @UniqueConstraint(columnNames = {"empresa_id", "codigo"})
)
public class ProductModel {

    public ProductModel(String codigo, String nombre, BigDecimal precioVenta, EnterpriseModel empresa) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precioVenta = precioVenta;
        this.empresa = empresa;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Codigo interno / SKU. Es unico por empresa, no global:
    // dos negocios distintos pueden usar el mismo codigo.
    @Column(nullable = false)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    // UNIDAD, KG, LITRO, CAJA, etc.
    @Column(name = "unidad_medida", nullable = false)
    private String unidadMedida = "UNIDAD";

    // Ultimo costo de compra (referencial para la orden de compra)
    @Column(name = "precio_compra", precision = 12, scale = 2)
    private BigDecimal precioCompra;

    // Precio de venta (referencial para la orden de venta)
    @Column(name = "precio_venta", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioVenta;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo = 0;

    // Borrado logico: un producto que ya aparece en ordenes no se elimina, se desactiva
    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    // Opcional: un producto puede no tener categoria asignada
    @ManyToOne
    @JoinColumn(name = "categoria_id")
    @JsonIgnoreProperties("empresa")
    private CategoryModel categoria;

    // Cada producto pertenece a una sola empresa (igual que Cliente)
    @ManyToOne(optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnoreProperties("proveedores")
    private EnterpriseModel empresa;

    // lifecycle methods
    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
        if (this.stock == null) this.stock = 0;
        if (this.stockMinimo == null) this.stockMinimo = 0;
        if (this.activo == null) this.activo = true;
        if (this.unidadMedida == null) this.unidadMedida = "UNIDAD";
    }

    // helper methods (inventario)

    // Entrada de mercaderia: al recibir una orden de compra
    public void aumentarStock(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        this.stock += cantidad;
    }

    // Salida de mercaderia: al confirmar una orden de venta
    public void disminuirStock(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        if (this.stock < cantidad) {
            throw new DataIntegrityViolationException(
                "Stock insuficiente para el producto " + codigo
                    + " (disponible: " + stock + ", solicitado: " + cantidad + ")"
            );
        }
        this.stock -= cantidad;
    }

    // Ajuste por inventario fisico: fija el stock al valor contado
    public void ajustarStock(int nuevoStock) {
        if (nuevoStock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        this.stock = nuevoStock;
    }

    public boolean isStockBajo() {
        return stock <= stockMinimo;
    }
}
