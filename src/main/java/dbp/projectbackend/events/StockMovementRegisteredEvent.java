package dbp.projectbackend.events;

import dbp.projectbackend.enums.MovementType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Se publica cuando un usuario registra a mano un movimiento de inventario
 * (entrada, salida, devolución o ajuste por conteo físico). Las compras y ventas
 * tienen sus propios eventos.
 */
@Getter
public class StockMovementRegisteredEvent extends ApplicationEvent {

    private final Long movimientoId;
    private final Long empresaId;
    private final Long varianteId;
    private final String varianteDescripcion;
    private final MovementType tipo;
    private final int cantidad;
    private final int stockAnterior;
    private final int stockResultante;
    private final String motivo;
    private final String registradoPor;

    public StockMovementRegisteredEvent(Object source, Long movimientoId, Long empresaId, Long varianteId,
                                        String varianteDescripcion, MovementType tipo, int cantidad,
                                        int stockAnterior, int stockResultante, String motivo,
                                        String registradoPor) {
        super(source);
        this.movimientoId = movimientoId;
        this.empresaId = empresaId;
        this.varianteId = varianteId;
        this.varianteDescripcion = varianteDescripcion;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.stockAnterior = stockAnterior;
        this.stockResultante = stockResultante;
        this.motivo = motivo;
        this.registradoPor = registradoPor;
    }

    /** Diferencia con signo: positiva si el stock subió, negativa si bajó. */
    public int getDiferencia() {
        return stockResultante - stockAnterior;
    }

    public boolean reducesStock() {
        return stockResultante < stockAnterior;
    }
}
