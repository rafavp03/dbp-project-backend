package dbp.projectbackend.events;

import dbp.projectbackend.models.Role;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UsuarioRegistradoEvent extends ApplicationEvent {

    private final String nombre;
    private final String email;
    private final String empresa;
    private final Role rol;

    public UsuarioRegistradoEvent(Object source, String nombre, String email, String empresa, Role rol) {
        super(source);
        this.nombre = nombre;
        this.email = email;
        this.empresa = empresa;
        this.rol = rol;
    }
}
