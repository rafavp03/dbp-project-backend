package dbp.projectbackend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity
@Table(name = "proveedores")
public class SupplierModel {

    public SupplierModel(String ruc, String razonSocial) {
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

    @Column(unique = true)
    private Integer telefono;

    @Column(unique = true)
    private String correo;

    @ManyToMany(mappedBy = "proveedores")
    @JsonIgnoreProperties("proveedores")
    private Set<EnterpriseModel> empresas = new HashSet<>();
}
