package dbp.projectbackend.models;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity
@Table(name = "consultas_ia")
public class ConsultaIAModel {

    public ConsultaIAModel(UserModel usuario, EnterpriseModel empresa, String pregunta, String respuesta, boolean exitosa) {
        this.usuario = usuario;
        this.empresa = empresa;
        this.pregunta = pregunta;
        this.respuesta = respuesta;
        this.exitosa = exitosa;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserModel usuario;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private EnterpriseModel empresa;

    @Column(nullable = false, length = 500)
    private String pregunta;

    @Column(length = 4000)
    private String respuesta;

    @Column(nullable = false)
    private Boolean exitosa;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    @PrePersist
    protected void onCreate() {
        this.fecha = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ConsultaIAModel other = (ConsultaIAModel) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
