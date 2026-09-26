package dbp.projectbackend.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class VentaRegistradaEvent extends ApplicationEvent {

    private final Long ventaId;
    private final Long empresaId;
    private final List<Long> varianteIds;

    public VentaRegistradaEvent(Object source, Long ventaId, Long empresaId, List<Long> varianteIds) {
        super(source);
        this.ventaId = ventaId;
        this.empresaId = empresaId;
        this.varianteIds = List.copyOf(varianteIds);
    }
}
