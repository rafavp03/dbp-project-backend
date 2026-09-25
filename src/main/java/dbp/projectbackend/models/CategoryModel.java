package dbp.projectbackend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity
@Table(
    name = "categorias",
    uniqueConstraints = @UniqueConstraint(columnNames = {"empresa_id", "nombre"})
)
public class CategoryModel {

    public CategoryModel(String nombre, EnterpriseModel empresa) {
        this.nombre = nombre;
        this.empresa = empresa;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unico por empresa: dos negocios pueden tener la categoria "Bebidas"
    @Column(nullable = false)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    // Borrado logico: una categoria con productos no se elimina, se desactiva
    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    // Cada categoria pertenece a una sola empresa
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnoreProperties("proveedores")
    private EnterpriseModel empresa;

    // lifecycle methods
    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
        if (this.activo == null) this.activo = true;
    }
}
