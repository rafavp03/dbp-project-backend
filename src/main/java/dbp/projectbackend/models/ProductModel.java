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

// Modelo / diseno de prenda (ej. "Polo basico cuello redondo").
// El stock no vive aqui sino en cada variante (talla + color).
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

    // Codigo interno del modelo. Es unico por empresa, no global.
    @Column(nullable = false)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    // UNIDAD, PAR, JUEGO, etc.
    @Column(name = "unidad_medida", nullable = false)
    private String unidadMedida = "UNIDAD";

    // Costo actual de compra. Se copia al detalle de cada venta para calcular la ganancia real.
    @Column(name = "precio_compra", precision = 12, scale = 2)
    private BigDecimal precioCompra;

    // Precio de lista. Se copia al detalle de cada venta para medir cuanto se rebajo.
    @Column(name = "precio_venta", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioVenta;

    // Borrado logico: un producto que ya aparece en ordenes no se elimina, se desactiva
    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    // Opcional: un producto puede no tener categoria asignada
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    @JsonIgnoreProperties("empresa")
    private CategoryModel categoria;

    // Cada producto pertenece a una sola empresa (igual que Cliente)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnoreProperties("proveedores")
    private EnterpriseModel empresa;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("producto")
    private List<VarianteProductoModel> variantes = new ArrayList<>();

    // lifecycle methods
    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
        if (this.activo == null) this.activo = true;
        if (this.unidadMedida == null) this.unidadMedida = "UNIDAD";
    }

    // helper methods
    public void addVariante(VarianteProductoModel variante) {
        variantes.add(variante);
        variante.setProducto(this);
    }

    public void removeVariante(VarianteProductoModel variante) {
        variantes.remove(variante);
        variante.setProducto(null);
    }

    // Suma del stock de todas las tallas y colores
    public int getStockTotal() {
        return variantes.stream()
                .mapToInt(VarianteProductoModel::getStock)
                .sum();
    }
}
