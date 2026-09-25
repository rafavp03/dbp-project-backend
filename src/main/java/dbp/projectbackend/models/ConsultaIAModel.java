package dbp.projectbackend.models;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

// Registro de cada pregunta hecha al asistente de IA.
// Sirve para el historial del usuario, para el limite diario y para revisar que se pregunto si algo sale mal.
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

    // Si se elimina el usuario, se eliminan tambien sus consultas
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

    // false si la IA no respondio (error del proveedor); esas no cuentan para el limite diario
    @Column(nullable = false)
    private Boolean exitosa;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    @PrePersist
    protected void onCreate() {
        this.fecha = LocalDateTime.now();
    }
}
