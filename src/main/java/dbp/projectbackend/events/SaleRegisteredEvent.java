package dbp.projectbackend.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class SaleRegisteredEvent extends ApplicationEvent {

    private final Long ventaId;
    private final Long empresaId;
    private final List<Long> varianteIds;

    public SaleRegisteredEvent(Object source, Long ventaId, Long empresaId, List<Long> varianteIds) {
        super(source);
        this.ventaId = ventaId;
        this.empresaId = empresaId;
        this.varianteIds = List.copyOf(varianteIds);
    }
}
