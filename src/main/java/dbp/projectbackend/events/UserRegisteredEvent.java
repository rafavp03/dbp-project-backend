package dbp.projectbackend.events;

import dbp.projectbackend.enums.Role;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserRegisteredEvent extends ApplicationEvent {

    private final String nombre;
    private final String email;
    private final String empresa;
    private final Role rol;

    public UserRegisteredEvent(Object source, String nombre, String email, String empresa, Role rol) {
        super(source);
        this.nombre = nombre;
        this.email = email;
        this.empresa = empresa;
        this.rol = rol;
    }
}
