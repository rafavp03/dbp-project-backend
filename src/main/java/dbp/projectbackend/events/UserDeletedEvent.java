package dbp.projectbackend.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Se publica cuando un ADMIN elimina la cuenta de un empleado.
 */
@Getter
public class UserDeletedEvent extends ApplicationEvent {

    private final String nombre;
    private final String email;
    private final String empresa;
    private final String eliminadoPor;

    public UserDeletedEvent(Object source, String nombre, String email, String empresa, String eliminadoPor) {
        super(source);
        this.nombre = nombre;
        this.email = email;
        this.empresa = empresa;
        this.eliminadoPor = eliminadoPor;
    }
}
