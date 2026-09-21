package dbp.projectbackend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity
@Table(name = "empresas")
public class EnterpriseModel {

    public EnterpriseModel(String ruc, String razonSocial) {
        this.ruc = ruc;
        this.razonSocial = razonSocial;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique = true)
    private String ruc;

    @Column(name="razon_social", nullable=false, unique=true)
    private String razonSocial;

    @Column(name = "fecha_registro", nullable=false, updatable=false)
    private LocalDateTime fechaRegistro;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonIgnoreProperties("empresas")
    @JoinTable(
        name = "empresa_proveedor",
        joinColumns = @JoinColumn(name = "empresa_id"),
        inverseJoinColumns = @JoinColumn(name = "proveedor_id")
    )
    private Set<SupplierModel> proveedores = new HashSet<>();

    // lifecycle methods
    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
    }

    // helper methods
    public void addSupplier(SupplierModel proveedor) {
        proveedores.add(proveedor);
        proveedor.getEmpresas().add(this);
    }

    public void removeSupplier(SupplierModel proveedor) {
        proveedores.remove(proveedor);
        proveedor.getEmpresas().remove(this);
    }
}
