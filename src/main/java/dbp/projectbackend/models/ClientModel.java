package dbp.projectbackend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity
@Table(
    name = "clientes",
    uniqueConstraints = @UniqueConstraint(columnNames = {"empresa_id", "documento"})
)
public class ClientModel {

    public ClientModel(String documento, String nombre, EnterpriseModel empresa) {
        this.documento = documento;
        this.nombre = nombre;
        this.empresa = empresa;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // DNI (8) o RUC (11). Es unico por empresa, no global:
    // la misma persona puede ser cliente de dos negocios distintos.
    @Column(nullable=false)
    private String documento;

    @Column(nullable=false)
    private String nombre;

    private Integer telefono;

    private String correo;

    private String direccion;

    @Column(name = "fecha_registro", nullable=false, updatable=false)
    private LocalDateTime fechaRegistro;

    // Cada cliente pertenece a una sola empresa (a diferencia de Proveedor, que es N:M)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnoreProperties("proveedores")
    private EnterpriseModel empresa;

    // lifecycle methods
    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ClientModel other = (ClientModel) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
